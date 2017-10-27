package fiji.plugin.filamentdetector.model;

import java.util.ArrayList;

public class Filaments extends ArrayList<Filament> {

	private static final long serialVersionUID = 1L;

	public int getIndexByID(int id) {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getId() == id) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		String out = "";
		for (Filament filament : this) {
			out += filament.toString() + "\n";
		}
		return out;
	}

	public String info() {
		String out = "";
		for (Filament filament : this) {
			out += filament.info() + "\n";
		}
		return out;
	}

	public Filaments simplify(double toleranceDistance) {

		Filaments newFilaments = new Filaments();

		for (Filament filament : this) {
			newFilaments.add(filament.simplify(toleranceDistance));
		}

		return newFilaments;

	}

	@Override
	public boolean add(Filament filament) {
		return super.add(filament);
	}

}
