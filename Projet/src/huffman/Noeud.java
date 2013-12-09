package huffman;

public class Noeud extends Element {

	private Element filsDroit, filsGauche;

	public Noeud(Element filsGauche, Element filsDroit) {
		super();
		this.filsGauche = filsGauche;
		this.filsDroit = filsDroit;
	}

	public void setFilsGauche(Element filsGauche) {
		this.filsGauche = filsGauche;
	}

	public void setFilsDroit(Element filsDroit) {
		this.filsDroit = filsDroit;
	}

	public Element getFilsDroit() {
		return filsDroit;
	}

	public Element getFilsGauche() {
		return filsGauche;
	}

}
