package project.hikerguide.ui.adapters.abstractadapters;

import android.support.v7.widget.RecyclerView;

import project.hikerguide.ui.adapters.interfaces.Hideable;

/**
 * Created by Alvin on 9/1/2017.
 */

public abstract class HideableAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> implements Hideable {
}
