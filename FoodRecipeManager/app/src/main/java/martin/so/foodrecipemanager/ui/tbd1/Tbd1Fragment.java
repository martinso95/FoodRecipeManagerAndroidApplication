package martin.so.foodrecipemanager.ui.tbd1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import martin.so.foodrecipemanager.R;

public class Tbd1Fragment extends Fragment {

    private View view = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_tbd1, container, false);
        }
        return view;
    }
}