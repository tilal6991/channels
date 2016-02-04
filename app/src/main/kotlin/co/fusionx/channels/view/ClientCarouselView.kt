package co.fusionx.channels.view

import android.content.Context
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import co.fusionx.channels.R
import co.fusionx.channels.viewmodel.persistent.ClientVM
import co.fusionx.channels.viewmodel.persistent.SelectedClientsVM
import com.amulyakhare.textdrawable.TextDrawable

/**
 * BUGS: selecting one image and then another very quickly
 */
public class ClientCarouselView(
        context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private lateinit var viewArray: Array<ImageView>

    val clickListener: View.OnClickListener = View.OnClickListener {
        when (it) {
            viewArray[1] -> selectedClientsVM?.selectPenultimate()
            viewArray[2] -> selectedClientsVM?.selectAntePenultimate()
        }
    }

    private var selectedClientsVM: SelectedClientsVM? = null
    private val callback = object : SelectedClientsVM.OnClientsChangedCallback {
        override fun onNewClientAdded() {
            resetClientViews()
        }

        override fun onLatestPenultimateSwap() {
            swapToFront(1)
        }

        override fun onLatestAntePenultimateSwap() {
            swapToFront(2)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        viewArray = arrayOf(
                findViewById(R.id.profile_image) as ImageView,
                findViewById(R.id.profile_image_2) as ImageView,
                findViewById(R.id.profile_image_3) as ImageView
        )
    }

    private fun swapToFront(newFrontIndex: Int) {
        val oldFront = viewArray[0]
        val newFront = viewArray[newFrontIndex]

        val frontX = ViewCompat.getX(oldFront)
        val frontY = ViewCompat.getX(oldFront)

        fadeAndMoveToNewPosition(oldFront, ViewCompat.getX(newFront), ViewCompat.getY(newFront))
        expandAndMoveToNewPosition(newFront, frontX, frontY)

        viewArray[0] = newFront
        viewArray[newFrontIndex] = oldFront
    }

    fun setAdapter(adapter: SelectedClientsVM) {
        selectedClientsVM?.removeOnClientsChangedCallback(callback)
        selectedClientsVM = adapter
        selectedClientsVM?.addOnClientsChangedCallback(callback)

        resetClientViews()
    }

    private fun resetClientViews() {
        val selectedClients = selectedClientsVM ?: return
        val latestClient = selectedClients.latest

        resetClientView(viewArray[0], latestClient)
        if (latestClient == null) {
            return
        }

        // Apply special scaling to view at position 0.
        ViewCompat.setScaleX(viewArray[0], 1.75f)
        ViewCompat.setScaleY(viewArray[0], 1.75f)

        resetClientView(viewArray[1], selectedClients.penultimate)
        resetClientView(viewArray[2], selectedClients.antepenultimate)
    }

    private fun resetClientView(imageView: ImageView, client: ClientVM?) {
        if (client == null) {
            hide(imageView)
            return
        }

        val drawable = TextDrawable.builder()
                .round()
                .build(client.name.subSequence(0, 2).toString(), resources.getColor(R.color.colorAccent))
        imageView.setImageDrawable(drawable)
        show(imageView)
    }

    private fun show(imageView: ImageView) {
        if (imageView.visibility == VISIBLE) {
            return
        }

        ViewCompat.animate(imageView)
                .alpha(1f)
                .setDuration(250)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withStartAction {
                    imageView.visibility = VISIBLE
                }
    }

    private fun hide(imageView: ImageView) {
        if (imageView.visibility == GONE) {
            return
        }

        ViewCompat.animate(imageView)
                .alpha(0f)
                .setDuration(250)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    imageView.visibility = GONE
                }
    }

    private fun expandAndMoveToNewPosition(view: View, oldCurrentX: Float, oldCurrentY: Float) {
        // Update the new elevation
        // ViewCompat.setElevation(view, 5f)

        // Expand to 1.75x times while moving to the new position and
        // readd the listener when done
        ViewCompat.animate(view)
                .scaleX(1.75f)
                .scaleY(1.75f)
                .x(oldCurrentX)
                .y(oldCurrentY)
                .setDuration(250)
                .setInterpolator(AccelerateDecelerateInterpolator())
    }

    private fun fadeAndMoveToNewPosition(currentView: ImageView,
                                         newCurrentX: Float,
                                         newCurrentY: Float) {
        // Do the fade with half the full animation time
        ViewCompat.animate(currentView)
                .alpha(0f)
                .setDuration(200)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    // Now move to the new position...
                    ViewCompat.setX(currentView, newCurrentX)
                    ViewCompat.setY(currentView, newCurrentY)

                    // ...reset the size...
                    ViewCompat.setScaleX(currentView, 1f)
                    ViewCompat.setScaleY(currentView, 1f)

                    // ...and the elevation
                    ViewCompat.setElevation(currentView, 0f)

                    // Fade the image back in and re-add the listener
                    ViewCompat.animate(currentView)
                            .setDuration(100)
                            .alpha(1f)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                }
    }
}