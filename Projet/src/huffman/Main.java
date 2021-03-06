package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {

		if (args.length == 3) {
			String option = args[0];
			File src = new File(args[1]), dest = new File(args[2]);
			if (option.equals("d")) {
				System.out.println("[Décompression...]");
				Huffman.decompression(new FileInputStream(src), dest);
				return;
			} else if (option.equals("c")) {
				System.out.println("[Compression...]");
				Huffman.compression(src, new FileOutputStream(dest));
				return;
			}

		}
		System.out.println("Usage : [c|d] fichier.src fichier.dest");
	}
}
