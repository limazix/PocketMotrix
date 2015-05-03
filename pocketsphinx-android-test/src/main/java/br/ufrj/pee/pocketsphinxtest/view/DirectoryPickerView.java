package br.ufrj.pee.pocketsphinxtest.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import br.ufrj.pee.pocketsphinxtest.R;

@EViewGroup(R.layout.directory_picker_view)
public class DirectoryPickerView extends RelativeLayout {

    private String directoryPath;
    private String directoryLabel;

    @ViewById(R.id.txtDirectoryLabel)
    TextView txtDirectoryLabel;

    @ViewById(R.id.txtDirectoryPath)
    TextView txtDirectoryPath;

    @AfterViews
    void initViews() {
        if(directoryLabel != null)
            setDirectoryLabel(directoryLabel);
    }

    public DirectoryPickerView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public DirectoryPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public DirectoryPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.DirectoryPickerView, defStyle, 0);

        try {
            directoryLabel = a.getString(R.styleable.DirectoryPickerView_txtDirectoryLabel);
        } finally {
            a.recycle();
        }
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
        txtDirectoryPath.setText(directoryPath);
        invalidate();
        requestLayout();
    }

    public String getDirectoryLabel() {
        return directoryLabel;
    }

    public void setDirectoryLabel(String directoryLabel) {
        this.directoryLabel = directoryLabel;
        txtDirectoryLabel.setText(directoryLabel);
    }
}
