package finalproject.entities;

public class Person implements java.io.Serializable {

	private static final long serialVersionUID = 4190276780070819093L;

	// this is a person object that you will construct with data from the DB
	// table. The "sent" column is unnecessary. It's just a person with
	// a first name, last name, age, city, and ID.
	private String first;
	private String last;
	private String city;
	private int age;
	private int sent;
	private int id;

	public Person(String first, String last, String city, int age, int sent, int id) {
		this.first = first;
		this.last = last;
		this.city = city;
		this.age = age;
		this.sent = sent;
		this.id = id;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getFirst() {
		return first;
	}

	public String getLast() {
		return last;
	}

	public String getCity() {
		return city;
	}

	public int getAge() {
		return age;
	}

	public int getSent() {
		return sent;
	}

	public int getId() {
		return id;
	}
}
