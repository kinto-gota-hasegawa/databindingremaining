package com.example.databindingremaining

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(
    private val onItemClick: ((User) -> Unit)? = null,
    private val onItemLongClick: ((User) -> Unit)? = null
) : ListAdapter<User, CustomAdapter.UserViewHolder>(UserDiffCallback()) {
    
    private var selectedPosition = RecyclerView.NO_POSITION
    private var isMultiSelectMode = false
    private val selectedItems = mutableSetOf<String>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, position == selectedPosition, selectedItems.contains(user.id))
    }
    
    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition)
        }
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position)
        }
    }
    
    fun toggleMultiSelectMode() {
        isMultiSelectMode = !isMultiSelectMode
        if (!isMultiSelectMode) {
            selectedItems.clear()
            notifyDataSetChanged()
        }
    }
    
    fun toggleItemSelection(userId: String) {
        if (selectedItems.contains(userId)) {
            selectedItems.remove(userId)
        } else {
            selectedItems.add(userId)
        }
        notifyItemChanged(currentList.indexOfFirst { it.id == userId })
    }
    
    fun getSelectedItems(): List<User> {
        return currentList.filter { selectedItems.contains(it.id) }
    }
    
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }
    
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val emailTextView: TextView = itemView.findViewById(R.id.textViewEmail)
        private val ageTextView: TextView = itemView.findViewById(R.id.textViewAge)
        private val profileImageView: ImageView = itemView.findViewById(R.id.imageViewProfile)
        private val statusIndicator: View = itemView.findViewById(R.id.viewStatusIndicator)
        private val initialsTextView: TextView = itemView.findViewById(R.id.textViewInitials)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val user = getItem(position)
                    
                    if (isMultiSelectMode) {
                        toggleItemSelection(user.id)
                    } else {
                        setSelectedPosition(position)
                        onItemClick?.invoke(user)
                    }
                }
            }
            
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val user = getItem(position)
                    
                    if (!isMultiSelectMode) {
                        toggleMultiSelectMode()
                        toggleItemSelection(user.id)
                    }
                    
                    onItemLongClick?.invoke(user)
                }
                true
            }
        }
        
        fun bind(user: User, isSelected: Boolean, isMultiSelected: Boolean) {
            nameTextView.text = user.getDisplayName()
            emailTextView.text = user.email
            ageTextView.text = "Age: ${user.age}"
            
            if (user.profileImageUrl.isNullOrEmpty()) {
                profileImageView.visibility = View.GONE
                initialsTextView.visibility = View.VISIBLE
                initialsTextView.text = user.getInitials()
            } else {
                profileImageView.visibility = View.VISIBLE
                initialsTextView.visibility = View.GONE
                loadImage(profileImageView, user.profileImageUrl)
            }
            
            statusIndicator.setBackgroundColor(
                if (user.isActive) {
                    itemView.context.getColor(android.R.color.holo_green_light)
                } else {
                    itemView.context.getColor(android.R.color.holo_red_light)
                }
            )
            
            when {
                isMultiSelected -> {
                    cardView.setCardBackgroundColor(
                        itemView.context.getColor(android.R.color.holo_blue_light)
                    )
                    cardView.alpha = 0.8f
                }
                isSelected -> {
                    cardView.setCardBackgroundColor(
                        itemView.context.getColor(android.R.color.darker_gray)
                    )
                    cardView.alpha = 0.9f
                }
                else -> {
                    cardView.setCardBackgroundColor(
                        itemView.context.getColor(android.R.color.white)
                    )
                    cardView.alpha = 1.0f
                }
            }
            
            itemView.contentDescription = "User ${user.name}, ${user.email}, Age ${user.age}"
        }
        
        private fun loadImage(imageView: ImageView, url: String) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }
    
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
    
    fun filterList(query: String) {
        val filteredList = if (query.isEmpty()) {
            currentList
        } else {
            currentList.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true)
            }
        }
        submitList(filteredList)
    }
    
    fun sortByName(ascending: Boolean = true) {
        val sortedList = if (ascending) {
            currentList.sortedBy { it.name.lowercase() }
        } else {
            currentList.sortedByDescending { it.name.lowercase() }
        }
        submitList(sortedList)
    }
    
    fun sortByAge(ascending: Boolean = true) {
        val sortedList = if (ascending) {
            currentList.sortedBy { it.age }
        } else {
            currentList.sortedByDescending { it.age }
        }
        submitList(sortedList)
    }
    
    fun sortByDate(ascending: Boolean = true) {
        val sortedList = if (ascending) {
            currentList.sortedBy { it.createdAt }
        } else {
            currentList.sortedByDescending { it.createdAt }
        }
        submitList(sortedList)
    }
}