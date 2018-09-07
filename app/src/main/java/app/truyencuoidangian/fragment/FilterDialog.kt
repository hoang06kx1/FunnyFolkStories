package app.truyencuoidangian.fragment

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.truyencuoidangian.MainActivity
import app.truyencuoidangian.R
import app.truyencuoidangian.repository.StoryDB
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_filter.*

class FilterDialog: DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_filter, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            this@FilterDialog.rb_group.clearCheck()
            when (currentFilter) {
                filterAllStories -> this@FilterDialog.rb_all.isChecked = true
                filterFolkStories -> this@FilterDialog.rb_folk.isChecked = true
                filterObsceneStories -> this@FilterDialog.rb_obscene.isChecked = true
                filterUnReadStories -> this@FilterDialog.rb_unread.isChecked = true
                filterReadStories -> this@FilterDialog.rb_read.isChecked = true
            }
        }

        bt_ok.setOnClickListener {
            (activity as MainActivity).ic_filter.setImageResource(R.drawable.ic_filter)
            if (rb_all.isChecked) {
                (activity as MainActivity).currentFilter = (activity as MainActivity).filterAllStories
                (activity as MainActivity).ic_filter.setImageResource(R.drawable.ic_filter_unselected)
            }
            if (rb_read.isChecked) {
                (activity as MainActivity).currentFilter = (activity as MainActivity).filterReadStories
            }
            if (rb_unread.isChecked) {
                (activity as MainActivity).currentFilter = (activity as MainActivity).filterUnReadStories
            }
            if (rb_obscene.isChecked) {
                (activity as MainActivity).currentFilter = (activity as MainActivity).filterObsceneStories
            }
            if (rb_folk.isChecked) {
                (activity as MainActivity).currentFilter = (activity as MainActivity).filterFolkStories
            }
            (activity as MainActivity).triggerReload()
            dismiss()
        }
    }
}