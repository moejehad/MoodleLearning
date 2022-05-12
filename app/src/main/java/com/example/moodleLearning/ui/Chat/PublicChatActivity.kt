package com.example.moodleLearning.ui.Chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bumptech.glide.Glide
import com.example.moodleLearning.R
import com.example.moodleLearning.databinding.ActivityPublicChatBinding
import com.example.moodleLearning.data.models.Message
import com.example.moodleLearning.utils.Helper
import com.example.moodleLearning.utils.Helper.Companion.log
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PublicChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPublicChatBinding
    private var cUser = Firebase.auth.currentUser
    private var mUsername = cUser?.displayName
    private var mSendButton: ImageButton? = null
    private var mMessageRecyclerView: RecyclerView? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mProgressBar: ProgressBar? = null
    private var mMessageEditText: EditText? = null
    private var mAddMessageImageView: ImageView? = null
    private val userToken = Firebase.messaging.token.toString()
    private var mFirebaseDatabaseReference: DatabaseReference? = null
    private lateinit var courseId: String
    private var mFirebaseAdapter: FirebaseRecyclerAdapter<Message, MessageViewHolder>? = null

    class MessageViewHolder(v: View?) : RecyclerView.ViewHolder(v!!) {
        var messageRoot = itemView.findViewById<View>(R.id.msgRoot) as LinearLayout
        var messageTextView = itemView.findViewById<View>(R.id.messageTextView) as TextView
        var messengerImageView = itemView.findViewById<View>(R.id.messengerImageView) as ImageView
        var messengerTextView = itemView.findViewById<View>(R.id.messengerTextView) as TextView
    }

    companion object {
        const val MESSAGES_CHILD = "PublicChats"
        const val COURSE_ID = "courseId"
        const val CHAT_TITLE = "chatTitle"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }

        courseId = intent.getStringExtra(COURSE_ID).toString()
        binding.tvChatTitle.text = "${intent.getStringExtra(CHAT_TITLE)} Chat"

        mProgressBar = binding.pb
        mMessageRecyclerView = binding.rvChats
        mLinearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mLinearLayoutManager!!.stackFromEnd = true
        mMessageRecyclerView!!.layoutManager = mLinearLayoutManager

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        val parser = SnapshotParser { dataSnapshot ->
            val message = dataSnapshot.getValue(Message::class.java)!!
            message.id = dataSnapshot.key
            message
        }

        val messagesRef = mFirebaseDatabaseReference!!.child(MESSAGES_CHILD).child(courseId)
        val options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(messagesRef, parser)
            .build()
        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {

            override fun onBindViewHolder(
                holder: MessageViewHolder,
                position: Int,
                message: Message
            ) {
                if (mProgressBar!!.visibility == View.VISIBLE) mProgressBar!!.visibility =
                    ProgressBar.INVISIBLE

                log(this@PublicChatActivity, "${message.id}")
                if (message.messengerId == cUser!!.uid) {
                    holder.messageRoot.gravity = Gravity.END
                    holder.messengerImageView.visibility = View.GONE
                    holder.messengerTextView.visibility = View.GONE
                }else {
                    holder.messengerTextView.visibility = View.VISIBLE
                }

                if (message.text != null) {
                    holder.messageTextView.text = message.text
                    holder.messengerTextView.text = message.name
                    holder.messageTextView.visibility = TextView.VISIBLE
                }
                holder.messengerTextView.text = message.name
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return MessageViewHolder(inflater.inflate(R.layout.item_message, parent, false))
            }
        }

        mFirebaseAdapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val messageCount: Int = mFirebaseAdapter!!.itemCount
                val lastVisiblePosition =
                    mLinearLayoutManager!!.findLastCompletelyVisibleItemPosition()
                if (lastVisiblePosition == -1 || positionStart >= messageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    mMessageRecyclerView!!.scrollToPosition(positionStart)
                }
            }
        })

        mMessageRecyclerView!!.adapter = mFirebaseAdapter
        mMessageEditText = binding.etMessage
        mMessageEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mSendButton!!.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        mSendButton = binding.ibSend
        mSendButton!!.setOnClickListener {
            val message = Message(cUser!!.uid, mMessageEditText!!.text.toString(), mUsername, null)
            mFirebaseDatabaseReference!!.child(MESSAGES_CHILD).child(courseId).push()
                .setValue(message)
            mMessageEditText!!.setText("")
        }

    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter?.stopListening()
    }

}
