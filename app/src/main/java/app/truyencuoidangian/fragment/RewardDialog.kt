package app.truyencuoidangian.fragment

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.truyencuoidangian.MainActivity
import app.truyencuoidangian.R
import kotlinx.android.synthetic.main.dialog_reward.*

class RewardDialog : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_reward, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bt_ok.setOnClickListener {
            dismiss()
        }
        bt_watch.setOnClickListener {
            (activity as MainActivity).showRewardAd()
        }
    }
}