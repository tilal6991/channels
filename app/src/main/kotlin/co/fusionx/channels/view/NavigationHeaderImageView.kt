package co.fusionx.channels.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

public class NavigationHeaderImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null) : ImageView(context, attrs) {

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        if (l == null) {
            isClickable = false
        }
    }
}