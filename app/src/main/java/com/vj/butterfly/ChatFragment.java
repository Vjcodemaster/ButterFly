package com.vj.butterfly;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import app_utility.DataBaseHelper;
import app_utility.DatabaseHandler;
import app_utility.MessageService;
import app_utility.OnChatInterfaceListener;
import app_utility.StaticReferenceClass;
import app_utility.TimestampUtil;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link app_utility.OnChatInterfaceListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment implements OnChatInterfaceListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public static OnChatInterfaceListener mListener;

    ImageButton ibEmoji, ibSend;

    EditText etEmoji;

    RecyclerView recyclerView;
    ChatRVAdapter chatRVAdapter;

    DatabaseHandler dbh;

    boolean isTypingInformed = false;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mListener = this;
        dbh = new DatabaseHandler(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {
        //ibEmoji = view.findViewById(R.id.ib_emoji);
        ibSend = view.findViewById(R.id.ib_send);
        etEmoji = view.findViewById(R.id.et_input_msg);

        etEmoji.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String sTyping = etEmoji.getText().toString();
                if (sTyping.length() == 1) {
                    if (MessageService.onChatInterfaceListener != null)
                        MessageService.onChatInterfaceListener.onChat("TYPING", "", 1, null);
                } else if (sTyping.length() == 0) {
                    if (MessageService.onChatInterfaceListener != null)
                        MessageService.onChatInterfaceListener.onChat("TYPING", "", 0, null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recyclerView = view.findViewById(R.id.rv_message);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        //mLinearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        //recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);

        ArrayList<DataBaseHelper> alDBMessages = new ArrayList<>(dbh.getMessages());
        chatRVAdapter = new ChatRVAdapter(getActivity(), recyclerView, alDBMessages);
        recyclerView.setAdapter(chatRVAdapter);

        /*ibEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sMsg = etEmoji.getText().toString();
                etEmoji.setText("");
                //String sTimeStamp = String.valueOf(TimestampUtil.getCurrentTimestamp());
                String sTimeStamp = String.valueOf(TimestampUtil.getCurrentTimestampStringFormat());
                int type = StaticReferenceClass.OUTGOING;
                int status = StaticReferenceClass.PENDING;

                String sPreviousDate = dbh.lastDate().split(" ")[0];
                String sToday = sTimeStamp.split(" ")[0];
                if(!sToday.equals(sPreviousDate)){
                    int msgType = StaticReferenceClass.DATE;
                    int msgStatus = StaticReferenceClass.DONT_SEND;
                    DataBaseHelper e = new DataBaseHelper(msgType, "", sToday, msgStatus);
                    updateMessageList(e);
                }
                //Log.e("asdasd", sTimeStamp);
                //Log.e("asdasd", TimestampUtil.getCurrentTimestampStringFormat());

                //String sFinalTime = sTimeStamp.split("\\s")[1].substring(0,5);
                //String sFinalTime = sTimeStamp.split("\\s")[1].substring(0,5);
                DataBaseHelper e = new DataBaseHelper(type, sMsg, sTimeStamp, status);
                updateMessageList(e);
            }
        });
    }

    private void updateMessageList(DataBaseHelper e) {
        this.chatRVAdapter.getDataset().add(e);
        this.chatRVAdapter.notifyItemInserted(recyclerView.getAdapter().getItemCount() - 1);
        this.recyclerView.scrollToPosition(this.chatRVAdapter.getItemCount() - 1);

        dbh.addDataToMessageTable(e);
        MessageService.onChatInterfaceListener.onChat("MESSAGE_SENT", e.get_message(), 0, null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChatInterfaceListener) {
            mListener = (OnChatInterfaceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onChat(String sCase, String sMessage, int nFlag, DataBaseHelper e) {
        switch (sCase) {
            case "UPDATE_MSG":
                this.chatRVAdapter.getDataset().add(e);
                this.chatRVAdapter.notifyItemInserted(recyclerView.getAdapter().getItemCount() - 1);
                this.recyclerView.scrollToPosition(this.chatRVAdapter.getItemCount() - 1);
                break;
            case "UPDATE_STATUS":
                /*this.chatRVAdapter.notifyItemRangeChanged(recyclerView.getAdapter().getItemCount() - nFlag, nFlag);
                this.recyclerView.scrollToPosition(this.chatRVAdapter.getItemCount() - 1);*/
                if (e != null) {
                    this.chatRVAdapter.getDataset().get(nFlag - 1).set_status(e.get_status());
                    this.chatRVAdapter.notifyItemChanged(nFlag - 1);
                }
                //this.recyclerView.scrollToPosition(this.chatRVAdapter.getItemCount() - 1);
                break;
        }
    }
}
