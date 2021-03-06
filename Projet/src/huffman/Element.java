package huffman;

public abstract class Element {

	protected Element suiv;
	protected Noeud pere;
	protected int poids;

	public Element() {
		this.poids = 1;
	}

	public void setPere(Noeud pere) {
		this.pere = pere;
	}

	public void setSuiv(Element suiv) {
		this.suiv = suiv;
	}

	public Noeud getPere() {
		return pere;
	}

	public int getPoids() {
		return poids;
	}

	public void setPoids(int poids) {
		this.poids = poids;
	}

	public Element getSuiv() {
		return suiv;
	}

	public void incrPoids() {
		this.poids++;
	}

	public boolean estFilsGauche() {
		return this == this.getPere().getFilsGauche();
	}
}
