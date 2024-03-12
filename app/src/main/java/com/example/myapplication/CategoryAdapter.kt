import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class CategoryAdapter(private var categories: MutableList<String> = mutableListOf()) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // 뷰홀더 클래스
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    // 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(itemView)
    }

    // 이벤트 리스너
    var onDeleteButtonClick: ((Int) -> Unit)? = null
    var onCategoryItemClick: ((String) -> Unit)? = null  // 수정된 부분

    // 뷰홀더에 데이터 바인딩
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryNameTextView.text = category

        // 폰트 적용
        val customFont = ResourcesCompat.getFont(holder.categoryNameTextView.context, R.font.font)
        holder.categoryNameTextView.setTypeface(customFont, Typeface.BOLD)

        // 삭제 버튼 클릭 시
        holder.deleteButton.setOnClickListener {
            onDeleteButtonClick?.invoke(position)
        }

        // 아이템 클릭 시 (새로 추가한 부분)
        holder.itemView.setOnClickListener {
            onCategoryItemClick?.invoke(category)
        }
    }

    // 데이터 개수 반환
    override fun getItemCount(): Int {
        return categories.size
    }

    // 카테고리 목록 업데이트
    fun updateCategories(newCategories: MutableList<String>) {
        categories = newCategories
        notifyDataSetChanged()
        Log.d("CategoryAdapter", "Updated categories: $categories")
    }

    // 카테고리 삭제
    fun removeCategory(position: Int) {
        if (position in 0 until categories.size) {
            categories.removeAt(position)
            notifyDataSetChanged()
        }
    }

    // 카테고리 목록 가져오기
    fun getCategories(): MutableList<String> {
        return categories
    }
}

