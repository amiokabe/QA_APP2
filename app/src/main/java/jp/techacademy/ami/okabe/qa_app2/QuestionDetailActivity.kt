package jp.techacademy.ami.okabe.qa_app2

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_send.*

class QuestionDetailActivity: AppCompatActivity(){

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mDataBaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>

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
        if (user == null) {
            val fabnotfav = findViewById<FloatingActionButton>(R.id.fabnotfav)
            fabnotfav.hide()

            val fabfav = findViewById<FloatingActionButton>(R.id.fabfav)
            fabfav.hide()
        } else {
            if (isFavourite() == 0) {
                //お気に入り登録を解除する
                fabfav.setOnClickListener{

                }
            } else if (isFavourite() == 1) {
                //お気に入り登録する
                fabnotfav.setOnClickListener {
                    val user = FirebaseAuth.getInstance().currentUser
                    val dataBaseReference = FirebaseDatabase.getInstance().reference
                    val favRef = dataBaseReference.child(FavouritesPATH).child(user!!.uid).child(mQuestion.genre.toString())
                        .child(mQuestion.questionUid)

                    val userMap = ObjectMapper().convertValue(user, Map::class.java)
                    val data = userMap<String, String>()

                    val title = mQuestion.title
                    val body = mQuestion.body
                    val sp = PreferenceManager.getDefaultSharedPreferences(this)
                    val name = sp.getString(NameKEY, "")

                    data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
                    data["title"] = title
                    data["body"] = body
                    data["name"] = name

                    favRef.updateChildren(data)
                }
            }
        }
    }

    //お気に入りに登録されているか調べ、ボタンの表示を変更する
    private fun isFavourite(): Int {
        val questionUid = mQuestion.questionUid

        for (questionUid in mQuestionArrayList) {
            if (dataSnapshot.key.equals(questionUid)) {
                fabfav.show()
                fabnotfav.hide()
                return 0
            } else {
                fabfav.hide()
                fabnotfav.show()
                return 1
            }
        }
    }
}