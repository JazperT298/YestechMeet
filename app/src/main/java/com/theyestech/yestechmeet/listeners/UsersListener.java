package com.theyestech.yestechmeet.listeners;


import com.theyestech.yestechmeet.models.Users;

public interface UsersListener {

    void initiateVideoMeeting(Users users);

    void initiateAudioMeeting(Users users);

    void onMultipleUsersAction(Boolean isMultipleUsersSelected);
}
