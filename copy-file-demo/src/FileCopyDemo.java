import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

interface FileCopyRunner {
    void copyFile(File source, File target);
}

public class FileCopyDemo {

    private static final int ROUND = 5;

    private static void benchmark(FileCopyRunner test, File source, File target) {
        long elapsed = 0L;
        for (int i = 0; i < ROUND; i++) {
            long startTime = System.currentTimeMillis();
            test.copyFile(source, target);
            elapsed += System.currentTimeMillis() - startTime;
        }
        System.out.println(test + ":" + elapsed / ROUND);
    }

    private static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        FileCopyRunner noBufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = new FileInputStream(source);
                    out = new FileOutputStream(target);
                    int result;
                    while((result = in.read()) != -1) {
                        out.write(result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(in);
                    close(out);
                }
            }

            @Override
            public String toString() {
                return "noBufferStreamCopy";
            }

        };

        FileCopyRunner bufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = new BufferedInputStream(new FileInputStream(source));
                    out = new BufferedOutputStream(new FileOutputStream(target));
                    byte[] buffer = new byte[1024];
                    int result;
                    while((result = in.read(buffer)) != -1) {
                        out.write(buffer, 0, result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(in);
                    close(out);
                }
            }
            @Override
            public String toString() {
                return "bufferStreamCopy";
            }
        };

        FileCopyRunner nioBufferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel in = null;
                FileChannel out = null;

                try {
                    in = new FileInputStream(source).getChannel();
                    out = new FileOutputStream(target).getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    while (in.read(buffer) != -1) {
                        // 指针指向读取的数据段，并且由读模式转成写模式
                       buffer.flip();
                        while (buffer.hasRemaining()) {
                            out.write(buffer);
                        }
                        // 由写模式转成读模式
                        buffer.clear();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public String toString() {
                return "nioBufferCopy";
            }
        };

        FileCopyRunner nioTranferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel in = null;
                FileChannel out = null;
                try {
                    in = new FileInputStream(source).getChannel();
                    out = new FileOutputStream(target).getChannel();
                    long transferred = 0L;
                    long size = in.size();
                    while (transferred != size) {
                        transferred = in.transferTo(0, size, out);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public String toString() {
                return "nioTranferCopy";
            }
        };
    }

}
