class UncontrolledFormatString {

    void uncontrolledFormatString() {
        String data = "foo";
        System.out.format(data);
        System.out.format("%s%n", data);

    }
}
