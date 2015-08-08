package me.li2.talkingbook21;

import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import me.li2.talkingbook21.data.TalkingBookChapter;

public class ChapterPageFragment extends Fragment implements OnClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = "ChapterPageFragment";
    private static final String EXTRA_TIMING_JSON_URI = "me.li2.talkingbook21.ChapterPageFragment.timing_json_uri";
    private static final String EXTRA_FROM_INDEX = "me.li2.talkingbook21.ChapterPageFragment.from_index";
    private static final String EXTRA_COUNT = "me.li2.talkingbook21.ChapterPageFragment.count";
    
    private Uri mJsonUri;
    private int mFromIndex;
    private int mCount;
    
    private TalkingBookChapter mChapter;
    private List<String> mWordList;
    private List<Integer> mTimingList;
    
    private TextView lastReadingWord;
    private OnWordClickListener mOnWordClickListener;
    
    public void setOnWordClickListener(OnWordClickListener l) {
        mOnWordClickListener = l;
    }
    
    public interface OnWordClickListener {
        public void onWordClick(int msec);
    }
    
    // Create fragment instance
    public static ChapterPageFragment newInstance(Uri jsonUri, int fromIndex, int count) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TIMING_JSON_URI, jsonUri.toString());
        args.putInt(EXTRA_FROM_INDEX, fromIndex);
        args.putInt(EXTRA_COUNT, count);
        
        ChapterPageFragment fragment = new ChapterPageFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJsonUri = Uri.parse(getArguments().getString(EXTRA_TIMING_JSON_URI));
        mFromIndex = getArguments().getInt(EXTRA_FROM_INDEX);
        mCount = getArguments().getInt(EXTRA_COUNT);
        mChapter = TalkingBookChapter.get(mJsonUri);
        mWordList = mChapter.getWordList(mFromIndex, mCount);
        mTimingList = mChapter.getTimingList(mFromIndex, mCount);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapter_page, container, false);
        
        ChapterPageUtil pageUtil = new ChapterPageUtil(getActivity());
        LinearLayout pageLayout = (LinearLayout) view.findViewById(R.id.rootLayout);
        LinearLayout lineLayout = new LinearLayout(getActivity());
        lineLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 64));
        pageLayout.addView(lineLayout);
        @SuppressWarnings("unused")
        int count = 1;
        
        int pageWidth = pageUtil.getPageWidth();
        int pageHeight = pageUtil.getPageHeight();
        int lineHeight = pageUtil.getLineHeight();
        int remainingWidth = pageWidth;
        int remainingHeight = pageHeight - lineHeight;
        
        for (int i = 0; i < mCount; i++) {
            // create a new TextView
            String word = mWordList.get(i);
            int timing = mTimingList.get(i);
            TextView wordTextView = createTextView(word, timing);
            int wordWidth = pageUtil.getStringWidth(word);
            if (wordWidth > remainingWidth) {
                lineLayout = new LinearLayout(getActivity());
                count++;
                lineLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 64));
                
                if (remainingHeight <= lineHeight) {
                    break;
                }
                pageLayout.addView(lineLayout);
                remainingWidth = pageWidth;
                remainingHeight -= lineHeight;
            }
            // add the TextView to the LinearLayout
            lineLayout.addView(wordTextView);
            remainingWidth -= wordWidth;
            // Log.d(TAG, String.format("line%d word%d %s wordwidth %d, remaining width %d, remaining height %d", count, i, word, wordWidth, remainingWidth, remainingHeight));
        }
        
        return view;
    }
    
    
    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            int msec = (int)v.getTag();
            if (mOnWordClickListener != null) {
                mOnWordClickListener.onWordClick(msec);
            }
        }
    }
    
    public void seekChapterToTime(int msec) {
        View page = getView();
        if (mTimingList == null || mTimingList.size() <= 0 || page == null) {
            return;
        }
        if (lastReadingWord != null) {
            lastReadingWord.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
            lastReadingWord = null;
        }
        int tag = findReadingWord(msec);
        TextView readingWord = (TextView) page.findViewWithTag(tag);
        if (readingWord != null) {
            readingWord.setBackgroundColor(getActivity().getResources().getColor(R.color.blue));
            lastReadingWord = readingWord;
        }
    }
    
    private int findReadingWord(int msec) {
        int nearestDiff = mTimingList.get(0);
        int nearestTag = mTimingList.get(0);
        int count = mTimingList.size();
        for (int index = 0; index < count; index++) {
            int nextTiming = mTimingList.get(index);
            int diff = Math.abs(nextTiming - msec);
            if (diff < nearestDiff) {
                nearestDiff = diff;
                nearestTag = nextTiming;
            }
        }
        return nearestTag;
    }
    
    private TextView createTextView(String word, int timing) {
        TextView aword = new TextView(getActivity());
        aword.setText(word);
        aword.setPadding(0, 0, ChapterPageUtil.DEFAULT_WORD_SPACE, 0);
        aword.setTextSize(ChapterPageUtil.DEFAULT_FONT_SIZE);
        aword.setTag(timing);
        aword.setOnClickListener(this);
        return aword;
    }    
}
