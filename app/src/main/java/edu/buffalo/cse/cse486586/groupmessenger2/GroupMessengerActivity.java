package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.test.LoaderTestCase;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;


public class GroupMessengerActivity extends Activity {

    private static final String TAG = "GroupMessengerActivity";
    public static final int SERVER_PORT = 10000;
    private static ContentValues contentValues;
    private final Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
    public static  int finalseq =0;
    public static int seq_num = 0;
    public static int proposal_seq_num=0;
    public static int received_num=0;
    public static int final_number=0;
    public static ArrayList allseq= new ArrayList();
    public static int nummsg=0;
    public static int count=0;
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    public static int failed_node=-1;
    public static ArrayList<message> message_queue1= new ArrayList<message>();
    public static ArrayList<Integer> portList = new ArrayList<Integer>();
    public  String mport;



    public class message {
        String msg;
        Double seqno;
        int id;
        boolean status;
        boolean failed;

        public message(String kmse_, boolean b, String i,int i1) {
            this.msg=kmse_;
            this.status=b;
            this.id=Integer.parseInt(i);
            this.seqno=Double.parseDouble(i1+"."+id);
            this.failed=false;

        }

        @Override
        public String toString(){
            return this.msg+":"+this.seqno+" "+this.id+" "+this.status;
        }
    }

    static class messageComparator implements Comparator<message> {
        public int compare(message m1, message m2) {
            if (m1.seqno < m2.seqno)
                return -1;
            else if (m1.seqno > m2.seqno)
                return 1;
            return 0;
        }
    }


    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        EditText et = (EditText) findViewById(R.id.editText1);
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        mport = myPort;
        portList.add(Integer.parseInt(REMOTE_PORT0));
        portList.add(Integer.parseInt(REMOTE_PORT1));
        portList.add(Integer.parseInt(REMOTE_PORT2));
        portList.add(Integer.parseInt(REMOTE_PORT3));
        portList.add(Integer.parseInt(REMOTE_PORT4));



        try{

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

        }
        catch (IOException e)
        {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        findViewById(R.id.button4).setOnClickListener(new OnSendClickListener(tv,et,getContentResolver(),myPort));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {


        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            Socket socket;
            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */


                synchronized (this) {
                    while (true) {
                        try
                        {
                        socket = serverSocket.accept(); // Connection Accepted
                        socket.setSoTimeout(500);
                        Log.d("Hey", "Connected");
                        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream()); // Output Stream to send ACK pack
                        DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream())); // Reading Data to read message from input stream
                        String msgRecv = in.readUTF();// Reads Message as UTF
                        //Log.e(TAG,"msgreceived"+msgRecv);
                        //Log.d(TAG, Boolean.toString(message_queue.contains(msgRecv)));
                        //Log.e("Message Queue",message_queue.toString());
                        proposal_seq_num=Math.max(proposal_seq_num,final_number);
                        if ((msgRecv.matches("[0-9]+\\.[0-9]{5}:.*"))) {
                            outStream.writeUTF(Integer.toString(proposal_seq_num));
                            message_queue1.add(new message(msgRecv.split(":")[1], false, (msgRecv.split(":")[0]).split("\\.")[1], proposal_seq_num));
                            proposal_seq_num++;
                            in.close();
                            outStream.close();
                            socket.close();

                        }
                        // msgRecv.;
                        else if(msgRecv.matches("[0-9]{5}")){

                                if(failed_node!=Integer.parseInt(msgRecv)) {
                                    failed_node = Integer.parseInt(msgRecv);
                                    try{
                                    for (int p : portList) {
                                        if (p != Integer.parseInt(mport) && p!=failed_node) {
                                            Log.e(TAG,"Sent fail");
                                            Socket s1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                                    p);
                                            s1.setSoTimeout(10000);
                                            DataOutputStream outputStream12 = new DataOutputStream(s1.getOutputStream());
                                            outputStream12.writeUTF(Integer.toString(failed_node));
                                            outputStream12.flush();
                                            outputStream12.close();
                                            s1.close();
                                        }
                                    }}catch (Exception e){
                                        continue;
                                    }
                                     Log.e("Failed node" , Integer.toString(failed_node));
                                }


                        }
                        else {
                            String msg = msgRecv.split(":")[0];
                            //Log.e(TAG, msgRecv);
                            final_number = Integer.parseInt(msgRecv.split(":")[1]);
                            for (message m : message_queue1) {

                                if (m.msg.equalsIgnoreCase(msg)) {
                                    m.seqno = Double.parseDouble(final_number + "." + m.id);
                                    m.status = true;
                                    //Log.e(TAG,message_queue1.toString());
                                }
                            }

                        }
                            Collections.sort(message_queue1, new messageComparator());

                            Log.e("TAG", message_queue1.toString());

                            ListIterator iter = message_queue1.listIterator();
                            while (iter.hasNext()) {
                                message m = (message) iter.next();
                                if (m.status == true ) {
                                    //Log.e("Delivered", m.msg);
                                    //Log.e("Delivered", Integer.toString(count));
                                    publishProgress(m.msg, Integer.toString(count));
                                    iter.remove();
                                    count++;
                                }
                                else if(m.id==failed_node && !m.status){
                                    publishProgress(m.msg,Integer.toString(count));
                                    count++;
                                    iter.remove();
                                }
                                else
                                    break;
                            }


                            allseq.add(final_number);
                            //Log.e(TAG, allseq.toString());
                            final_number++;//Message published to UI
                            outStream.writeUTF("ACK"); // ACK PAcket being sent
                            //Log.d(TAG, msgRecv);
                            in.close();
                            outStream.close();
                            socket.close();
                    }catch (Exception e){
                          continue;
                    }
                }
            }
        }


        protected void onProgressUpdate(String...strings) {

            String strReceived = strings[0].trim();
            String seqNum = strings[1].trim();
            TextView tv= (TextView) findViewById(R.id.textView1);
            tv.append(strReceived);
            tv.append("\n");
            ContentResolver cr = getContentResolver();
            contentValues = new ContentValues();
            contentValues.put("key", String.valueOf(seqNum));
            contentValues.put("value", strReceived);
            cr.insert(mUri,contentValues);
            return;
        }
    }

    public class OnSendClickListener implements View.OnClickListener {


        static final int SERVER_PORT = 10000;


        private final EditText Ed1;
        private final ContentResolver mContentResolver;
        private final Uri mUri;
        private final String myPort;


        public OnSendClickListener(TextView _tv, EditText et, ContentResolver _cr, String myP) {

            Ed1 = et;
            mContentResolver = _cr;
            mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
            myPort = myP;
        }

        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }

        @Override
        public void onClick(View v) {

            try {

                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, Ed1.getText().toString(), myPort);
                Ed1.getText().clear();
            } catch (Exception e) {

                //Log.d("Hey", "Failed");
                return;
            }

        }

        private class ClientTask extends AsyncTask<String, Void, Void> {

            @Override
            protected Void doInBackground(String... msgs) {
                        String msgToSend = msgs[0];
                        StringBuilder msgwithseq = new StringBuilder();
                        StringBuilder finalmsg = new StringBuilder();
                        ArrayList recv = new ArrayList();

                        msgwithseq.append(seq_num);
                        msgwithseq.append(".");
                        msgwithseq.append(myPort);
                        msgwithseq.append(":");
                        msgwithseq.append(msgToSend);
                        Log.d(TAG, msgwithseq.toString());

                        int Port = Integer.parseInt(REMOTE_PORT0);

                        ListIterator<Integer> plIterator = portList.listIterator();


                        do {
                            try {
                                Port=plIterator.next();
                                Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Port);
                                socket0.setSoTimeout(10000);
                                DataOutputStream outStream0 = new DataOutputStream(socket0.getOutputStream()); // Output Stream of the connection
                                outStream0.writeUTF(msgwithseq.toString()); // Message is written to the output stream which seeps through the socket


                                DataInputStream in0 = new DataInputStream(new BufferedInputStream(socket0.getInputStream())); // Input Stream to Receive Ack Packet
                                String ack0 = in0.readUTF();

                                StringBuilder ack = new StringBuilder(ack0);
                                received_num = Integer.parseInt(ack.toString());

                                recv.add(received_num);


                                //Log.d(TAG, ack.toString());
                                if (received_num >= 0) // Streams are Flushes and CLosed. Connection Ends
                                {
                                    outStream0.flush();
                                    in0.close();
                                    outStream0.close();
                                    socket0.close();
                                    ack.setLength(0);
                                }
                                ack.setLength(0);
                            }
                            catch (Exception e)
                            {
                                Log.e("Port List", portList.toString());

                                try{
                                    for(int p:portList)
                                    {
                                        Socket s= new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                                p);
                                        s.setSoTimeout(10000);
                                        DataOutputStream outputStream1= new DataOutputStream(s.getOutputStream());
                                        outputStream1.writeUTF(Integer.toString(Port));
                                        s.close();
                                        outputStream1.close();
                                    }
                                }catch(IOException e1){

                                    Log.e("Cannot","Connect");
                                    continue;
                                }
                                continue;

                            }
                            /*
                             * TODO: Fill in your client code that sends out a message.
                             */


                        }while(plIterator.hasNext());

                        plIterator=portList.listIterator();

                        Log.e(TAG, recv.toString());
                        seq_num = (Integer)Collections.max(recv);
                        Port = Integer.parseInt(REMOTE_PORT0);

                        finalmsg.append(msgToSend);
                        finalmsg.append((":"));
                        finalmsg.append(seq_num);

                        //Log.e(TAG, finalmsg.toString());
                        do{
                            try {
                                Port = plIterator.next();
                                Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Port);
                                socket0.setSoTimeout(10000);
                                /*
                                 * TODO: Fill in your client code that sends out a message.
                                 */

                                DataOutputStream outStream0 = new DataOutputStream(socket0.getOutputStream()); // Output Stream of the connection
                                outStream0.writeUTF(finalmsg.toString()); // Message is written to the output stream which seeps through the socket


                                DataInputStream in0 = new DataInputStream(new BufferedInputStream(socket0.getInputStream())); // Input Stream to Receive Ack Packet
                                String ack0 = in0.readUTF();

                                StringBuilder ack = new StringBuilder(ack0);

                                //Log.d(TAG, ack.toString());
                                if (ack.toString() == "ACK") // Streams are Flushes and CLosed. Connection Ends
                                {
                                    outStream0.flush();
                                    in0.close();
                                    outStream0.close();
                                    socket0.close();
                                    ack.setLength(0);
                                }
                            }catch (Exception e)
                            {
                                Log.e("Port List", portList.toString());

                                try{
                                    for(int p:portList)
                                    {
                                        Socket s= new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                                p);
                                        s.setSoTimeout(10000);
                                        DataOutputStream outputStream1= new DataOutputStream(s.getOutputStream());
                                        outputStream1.writeUTF(Integer.toString(Port));
                                        s.close();
                                        outputStream1.close();
                                    }
                                }catch(IOException e1){

                                    Log.e("Cannot","Connect");
                                    continue;
                                }
                                continue;

                            }
                        }while(plIterator.hasNext());


                return null;
            }


        }
    }
}
