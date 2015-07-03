/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app.cts;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.cts.stub.R;

public class ListActivityTestHelper extends ListActivity {
    public ListView listView;
    public View view;
    public int itemPosition;
    public long itemId;
    public boolean isOnContentChangedCalled = false;
    public static boolean isOnRestoreInstanceStateCalled = false;
    public boolean isSubActivityFinished = false;

    private static final int WAIT_BEFORE_FINISH = 1;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        listView = l;
        view = v;
        itemPosition = position;
        itemId = id;
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setListAdapter(new ArrayAdapter<String>(this,
                R.layout.list_activity_layout, STRING_ITEMS));
        Intent intent = new Intent(TestedScreen.WAIT_BEFORE_FINISH);
        intent.setClass(this, LocalScreen.class);
        startActivityForResult(intent, WAIT_BEFORE_FINISH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case WAIT_BEFORE_FINISH:
                isSubActivityFinished = true;
                break;
            default:
                break;
        }
    }

    public static final String[] STRING_ITEMS = {
            "Item 0", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5"
    };

    @Override
    public void onContentChanged() {
        isOnContentChangedCalled = true;
        super.onContentChanged();
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        isOnRestoreInstanceStateCalled = true;
        super.onRestoreInstanceState(state);
    }

}
