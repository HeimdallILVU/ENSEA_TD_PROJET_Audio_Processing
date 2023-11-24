package math;

public class Complex {
    private final double real;
    private final double imag;

    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public static Complex[] fromArray(double[] array) {
        Complex[] result = new Complex[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = new Complex(array[i], 0);
        }
        return result;
    }

    public double real() {
        return real;
    }

    public double imag() {
        return imag;
    }

    public Complex plus(Complex b) {
        return new Complex(this.real + b.real, this.imag + b.imag);
    }

    public Complex minus(Complex b) {
        return new Complex(this.real - b.real, this.imag - b.imag);
    }

    public Complex times(Complex b) {
        return new Complex(this.real * b.real - this.imag * b.imag, this.real * b.imag + this.imag * b.real);
    }

    public Complex conjugate() {
        return new Complex(this.real, -this.imag);
    }

    public double abs() {
        return Math.sqrt(this.real * this.real + this.imag * this.imag);
    }


}
