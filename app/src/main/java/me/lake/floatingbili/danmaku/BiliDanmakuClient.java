package me.lake.floatingbili.danmaku;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by lake on 15-11-26.
 */
public class BiliDanmakuClient {
    private IIncomingDanmakuCallback incomingDanmakuCallback;

    BiliDanmakuClient() {
    }
    public void setIncomingDanmakuCallback(IIncomingDanmakuCallback callback)
    {
        incomingDanmakuCallback = callback;
    }

    public void start() {

    }

    public void stop() {
    }
    class GetDanmakuThread extends Thread
    {
        GetDanmakuThread(int roomId)
        {

        }
        @Override
        public void run() {
            try {
                System.out.println("in");
                Socket client = new Socket("livecmt-1.bilibili.com", 88);
                System.out.println("in1");
                final DataOutputStream output = new DataOutputStream(client.getOutputStream());
                int roomid = 1029;
                output.write(new byte[]{0x01, 0x01, 0x00, 0x0c, (byte) (roomid >> 24), (byte) (roomid >> 16), (byte) (roomid >> 8), (byte) roomid,
                        0x00, 0x00, 0x00, 0x00});
                DataInputStream input = new DataInputStream(client.getInputStream());
                byte[] buf = new byte[1024];
                new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            long time = System.currentTimeMillis();
                            try {
                                output.write(new byte[] { 0x01, 0x02, 0x00, 0x04 });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            time = System.currentTimeMillis();
                            System.out.println("write");
                            try {
                                sleep(30000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
                while (true) {

                    try {
                        short type = input.readShort();
                        if (type == -1) {
                            break;
                        }
                        if (type == 1) {
                            int peoplenum = input.readInt();
                            System.out.println("peoplenum=" + peoplenum);
                        } else if (type == 4) {
                            short leftsize = input.readShort();
                            leftsize -= 4;
                            input.read(buf, 0, leftsize);
                            String s = new String(buf, 0, leftsize);
                            System.out.println("char=" + s);
                        } else {
                            System.out.println("!!!!!!!!!!!!type=" + type);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }

                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}