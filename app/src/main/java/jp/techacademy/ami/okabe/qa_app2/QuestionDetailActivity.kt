package jp.techacademy.ami.okabe.qa_app2

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_send.*
import java.util.*

class QuestionDetailActivity: AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mDataBaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private var mGenre: Int = 0

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        val extras = intent.extras
        mQuestion = extras.get("question") as Question
        mGenre = extras.getInt("genre")

        title = mQuestion.title

        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef =
            dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid)
                .child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        val user = FirebaseAuth.getInstance().currentUser
        val favRef = dataBaseReference.child(FavouritesPATH).child(user!!.uid).child(mQuestion.questionUid)
        if (user == null) {
            val fabnotfav = findViewById<FloatingActionButton>(R.id.fabnotfav)
            fabnotfav.hide()

            val fabfav = findViewById<FloatingActionButton>(R.id.fabfav)
            fabfav.hide()
        } else {
            val favRef = dataBaseReference.child(FavouritesPATH).child(user!!.uid).child(mQuestion.questionUid)

            favRef!!.addChildEventListener(childEventListener)

            if (favRef == null) {
                fabnotfav.show()
                fabfav.hide()

                //お気に入り登録する
                fabnotfav.setOnClickListener { v ->
                    Snackbar.make(v, "お気に入りに登録しました", Snackbar.LENGTH_LONG).show()

                    val data = HashMap<String, String>()

                    data["genre"] = mGenre.toString()
                    data["questionUid"] = mQuestion.questionUid.toString()

                    favRef.setValue(data)
                }
            } else {
                fabfav.show()
                fabnotfav.hide()

                //お気に入り登録を解除する
                fabfav.setOnClickListener{ v ->
                    Snackbar.make(v, "お気に入りから削除しました", Snackbar.LENGTH_LONG).show()



                }
            }
        }
    }

    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }
}