package com.netease.nim.uikit.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.sys.ActionBarUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.team.helper.AnnouncementHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.Team;

/**
 * 创建群公告界面
 * Created by hzxuwen on 2015/3/18.
 */
public class AdvancedTeamCreateAnnounceActivity extends TActionBarActivity {

    // constant
    private final static String EXTRA_TID = "EXTRA_TID";

    // data
    private String teamId;
    private String announce;

    // view
    private EditText teamAnnounceTitle;
    private EditText teamAnnounceContent;

    public static void startActivityForResult(Activity activity, String teamId, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(activity, AdvancedTeamCreateAnnounceActivity.class);
        intent.putExtra(EXTRA_TID, teamId);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_advanced_team_create_announce);
        setTitle(R.string.team_annourcement);

        parseIntentData();
        findViews();
        initActionbar();
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_TID);
    }

    private void findViews() {
        teamAnnounceTitle = (EditText) findViewById(R.id.team_announce_title);
        teamAnnounceContent = (EditText) findViewById(R.id.team_announce_content);
        teamAnnounceTitle.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
        teamAnnounceContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1024)});
    }

    private void initActionbar() {
        ActionBarUtil.addRightClickableTextViewOnActionBar(this, R.string.save, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAnnounceData();
            }
        });
    }

    private void requestAnnounceData() {
        if (!NetworkUtil.isNetAvailable(this)) {
            Toast.makeText(this, R.string.network_is_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(teamAnnounceTitle.getText().toString())) {
            Toast.makeText(AdvancedTeamCreateAnnounceActivity.this, R.string.team_announce_notice, Toast.LENGTH_SHORT).show();
            return;
        }

        ActionBarUtil.setTextViewEnable(this, false);
        // 请求群信息
        Team t = TeamDataCache.getInstance().getTeamById(teamId);
        if (t != null) {
            updateTeamData(t);
            updateAnnounce();
        } else {
            NIMClient.getService(TeamService.class).queryTeam(teamId).setCallback(new RequestCallback<Team>() {
                @Override
                public void onSuccess(Team param) {
                    updateTeamData(param);
                    updateAnnounce();
                    TeamDataCache.getInstance().addOrUpdateTeam(param);
                }

                @Override
                public void onFailed(int code) {
                    ActionBarUtil.setTextViewEnable(AdvancedTeamCreateAnnounceActivity.this, true);
                }

                @Override
                public void onException(Throwable exception) {
                    ActionBarUtil.setTextViewEnable(AdvancedTeamCreateAnnounceActivity.this, true);
                }
            });
        }
    }

    /**
     * 获得最新公告内容
     * @param team 群
     */
    private void updateTeamData(Team team) {
        if (team == null) {
            Toast.makeText(this, getString(R.string.team_not_exist), Toast.LENGTH_SHORT).show();
            showKeyboard(false);
            finish();
        } else {
            announce = team.getAnnouncement();
        }
    }

    /**
     * 创建公告更新到服务器
     */
    private void updateAnnounce() {
        String announceMent = AnnouncementHelper.makeAnnounceJson(teamId, announce, teamAnnounceTitle.getText().toString(),
                teamAnnounceContent.getText().toString());
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.Announcement, announceMent).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                setResult(Activity.RESULT_OK);
                showKeyboard(false);
                finish();
                Toast.makeText(AdvancedTeamCreateAnnounceActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                ActionBarUtil.setTextViewEnable(AdvancedTeamCreateAnnounceActivity.this, true);
                Toast.makeText(AdvancedTeamCreateAnnounceActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
                ActionBarUtil.setTextViewEnable(AdvancedTeamCreateAnnounceActivity.this, true);
                Toast.makeText(AdvancedTeamCreateAnnounceActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        showKeyboard(false);
        super.onBackPressed();
    }
}
