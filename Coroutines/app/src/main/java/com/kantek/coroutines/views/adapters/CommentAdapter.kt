package com.kantek.coroutines.views.adapters

import android.support.core.base.RecyclerAdapter
import android.support.core.base.RecyclerHolder
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.kantek.coroutines.R
import com.kantek.coroutines.models.Comment
import kotlinx.android.synthetic.main.item_view_comment.view.*

class CommentAdapter(view: RecyclerView) : RecyclerAdapter<Comment>(view) {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int)=object:
        RecyclerHolder<Comment>(p0, R.layout.item_view_comment){
        override fun bind(item: Comment) {
            super.bind(item)
            itemView.apply {
                txtName.text = item.name
                txtEmail.text = item.email
                txtComment.text = item.comment
            }
        }
    }

}