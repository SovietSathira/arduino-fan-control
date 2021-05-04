package eecs;

import jssc.SerialPort;
import jssc.SerialPortException;
import java.io.OutputStream;

public class SerialOutput extends OutputStream {
    public SerialPort sp = null;
    public SerialOutput(SerialPort port) {
        sp = port;
    }

    @Override
    public void write(int data) {
        if (sp != null && sp.isOpened()) {
            try {
                sp.writeByte((byte)(data & 0xFF));
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }
}
