package array;
import gov.nasa.jpf.vm.Verify;

public class ArrayJPF_Core {

	public static void main(String[] args) {
		Verify.resetInstructionCounter();
		new ArrayJPF_Core(Integer.parseInt(args[0]));
	}

	static boolean a1 = true;
	static boolean a2 = true;
	static boolean a3 = true;
	static boolean a4 = true;
	static boolean a5  = true;
	static boolean a6  = true;
	static boolean a7  = true;
	static boolean a8  = true;
	static boolean a9  = true;
	static boolean a10 = true;
	static boolean a11 = true;
	static boolean a12 = true;
	static boolean a13 = true;
	static boolean a14 = true;
	static boolean a15  = true;
	static boolean a16  = true;
	static boolean a17  = true;
	static boolean a18  = true;
	static boolean a19  = true;
	static boolean a20 = true;
	static boolean a21 = true;
	static boolean a22 = true;
	static boolean a23 = true;
	static boolean a24 = true;
	static boolean a25  = true;
	static boolean a26  = true;
	static boolean a27  = true;
	static boolean a28  = true;
	static boolean a29  = true;
	static boolean a30 = true;
	
	int[] array;
	public ArrayJPF_Core(int max) {
		array = new int[max + 1];
		int i = 0;
		if (i == max) return;
		if (a1 =  gb()) {array[i] = 1;} if (++i == max) return;
		if (a2 =  gb()) {array[i] = 1;} if (++i == max) return;
		if (a3 =  gb()) {array[i] = 1;} if (++i == max) return;
		if (a4 =  gb()) {array[i] = 1;} if (++i == max) return;
		if (a5 =  gb()) {array[i] = 1;} if (++i == max) return;
		if (a6 =  gb()) {array[i] = 1;} if (++i == max) return;
		if (a7 =  gb()) {array[i] = 1;} if (++i == max) return;
		if (a8 =  gb()) {array[i] = 1;} if (++i == max) return;
		if (a9 =  gb()) {array[i] = 1;} if (++i == max) return;
		if (a10 = gb()) {array[i] = 1;} if (++i == max) return;
		     
		if (a11 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a12 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a13 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a14 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a15 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a16 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a17 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a18 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a19 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a20 = gb()) {array[i] = 1;} if (++i == max) return;
		   
		if (a21 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a22 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a23 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a24 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a25 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a26 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a27 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a28 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a29 = gb()) {array[i] = 1;} if (++i == max) return;
		if (a30 = gb()) {array[i] = 1;} if (++i == max) return;
	}

	private boolean gb() {
		return Verify.getBoolean();
	}
	
}
