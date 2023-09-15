package com.example.macc_project.photoHunt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.macc_project.R

class ScoreboardAdapter(private val users: List<ScoreboardActivity.User>) : RecyclerView.Adapter<ScoreboardAdapter.ViewHolder>() {

    class ViewHolder(userView: View) : RecyclerView.ViewHolder(userView) {
        val position: TextView = userView.findViewById(R.id.tvPosition)
        val username: TextView = userView.findViewById(R.id.tvUser)
        val points: TextView = userView.findViewById(R.id.tvDescUserPoint)
        val pt: TextView = userView.findViewById(R.id.tvPt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_user_score, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.position.text = (position + 1).toString()
        holder.username.text = user.username
        holder.points.text = user.points.toString()
        holder.pt.text ="pt"
    }

    override fun getItemCount(): Int {
        return users.size
    }
}