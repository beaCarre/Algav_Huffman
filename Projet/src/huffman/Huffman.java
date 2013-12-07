package huffman;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class Huffman {

	// traiter l'ascii et le 26 char
	public static void compression(String texte, OutputStream out)
			throws IOException {
		// initialise l'arbre avec la feuille '#'
		ArbreHuffman arbre = new ArbreHuffman();
		GestionnaireBit gestion = new GestionnaireBit();

		for (char symbole : texte.toCharArray()) {

			if (arbre.estPresent(symbole)) {
				gestion.transmettreChemin(arbre.getCode(symbole)); //log n
 			} else {
				gestion.transmettreChemin(arbre.getCode('#')); // log n
				gestion.transmettreSymbole(symbole);
			}

			arbre.modification(symbole);
		}
		gestion.finirTransmission(arbre.getRacine().getPoids());

		gestion.ecrireDansFichier(out);
		System.out.println("[DEBUG] Arbre final compression : \n" + arbre);
		System.out.println("[DEBUG] Nombre de caracteres transmis : "
				+ arbre.getRacine().getPoids());
	}

	public static void compression(File file, OutputStream out)
			throws IOException {
		final int length = (int) file.length();
		final char[] buffer = new char[length];
		final FileReader fr = new FileReader(file);
		int offset = 0;
		while (offset < length) {
			int read = fr.read(buffer, offset, length - offset);
			offset += read;
		}

		compression(new String(buffer), out);
	}

	public static String decompression(InputStream in) throws IOException {
		StringBuilder str = new StringBuilder();
		ArbreHuffman arbre = new ArbreHuffman();

		// lecture des 4 premiers octets pour connaitre la taille
		int longueurDuTexte = 0;
		for (int i = 0; i < 4; i++) {
			longueurDuTexte += (in.read() << (24 - i * 8));
		}

		// Si il n'y a rien a lire, on s'arrete la
		if (longueurDuTexte == 0)
			return "";

		// Premier octet = lettre
		byte b = (byte) in.read();
		str.append((char) b);
		arbre.modification((char) b);

		LinkedList<Boolean> buffer = new LinkedList<Boolean>();
		int i = 1;
		while (i < longueurDuTexte) {

			// On lit les bits tant que le chemin n'est pas complet
			while (!arbre.estCheminComplet(buffer)) {
				buffer.addAll(GestionnaireBit.byteToBooleanList((byte) in
						.read()));
			}
			// Modifie la liste des bits lus - enlève les bits du buffer
			char symbole = arbre.recupererFeuille(buffer);
			if (symbole == '#') {
				// Si le prochain caractere inconnu n'est pas en memoire, on lit
				// le suivant
				if (buffer.size() < 8) {
					buffer.addAll(GestionnaireBit.byteToBooleanList((byte) in
							.read()));
				}
				symbole = GestionnaireBit.booleanListToChar(buffer);
			}

			str.append(symbole);
			arbre.modification(symbole);
			i++;
		}

		System.out.println("[DEBUG] Arbre final decompression : \n" + arbre);
		System.out.println("[DEBUG] Nombre de caracteres lu : " + i);

		return str.toString();
	}

	public static void decompression(InputStream in, File out)
			throws IOException {
		FileWriter fw = new FileWriter(out);
		fw.write(decompression(in));
		fw.flush();
	}
}
