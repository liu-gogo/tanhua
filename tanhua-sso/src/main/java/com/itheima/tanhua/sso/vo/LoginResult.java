package com.itheima.tanhua.sso.vo;

import lombok.Data;

@Data
public class LoginResult {

    private String token;

    private boolean isNew;


    public void setIsNew(boolean isNew){
        this.isNew = isNew;
    }

    public boolean getIsNew(){
        return isNew;
    }
}
