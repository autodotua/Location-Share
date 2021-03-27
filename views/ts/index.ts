

import $ from "jquery";
import "jquery-ui/ui/widgets/dialog"
import { OpenLayersMap } from './map';
import { User, Location } from './common';
import { postSignIn, postSignUp, postUserInfo, postGetAll, postCheckToken } from "./net";
import { Md5 } from 'ts-md5/dist/md5';

let map: OpenLayersMap;
let dialog: JQuery<HTMLElement>
$(() => {
    dialog = $("#dialog-login");
    dialog.dialog({
        autoOpen: false,
        closeOnEscape: false,
        open: function (event, ui) {
            $(".ui-dialog-titlebar-close").hide();
        },
        modal: true,
    });
    if (User.getCurrent() == null) {
        showSignInDialog();
    }
    else {
        postCheckToken((succeed, message, data) => {
            if (!succeed) {
                if (!message.startsWith("通信失败")) {
                    alert(message + "，请重新登录");
                    showSignInDialog();
                }
            }
        })
    }

    if (!navigator.geolocation) {
        // alert("浏览器不支持定位！");
    }

    map = new OpenLayersMap();

    setGroupText();
    $("#btn-location").click(() => {
        if (map.lastPoint != null) {
            map.to(map.lastPoint)
        }
        else
        {
            alert("还没有定位，请稍等")
        }
    })
    $("#nav-change-name").click(() => {
        let newName = prompt("请输入新的昵称：", User.getCurrent()!.displayName)
        if (newName != null) {
            let user = new User();
            user.displayName = newName;
            postUserInfo(user, (succeed, data) => {
                if (!succeed) {
                    alert("修改失败");
                }
                else {
                    User.getCurrent()!.displayName = newName!;
                    User.applyChange();
                }
            });
        }

    });
    $("#nav-change-group").click(() => {
        let newName = prompt("请输入新的组名：", User.getCurrent()!.groupName)
        if (newName != null) {
            let user = new User();
            user.groupName = newName;
            postUserInfo(user, (succeed, data) => {
                if (!succeed) {
                    alert("修改失败");
                }
                else {
                    User.getCurrent()!.groupName = newName!;
                    User.applyChange();
                    setGroupText();
                }
            });
        }


    });
    $("#nav-positionning").click(() => {
        if (localStorage.getItem("positionning") == "false") {
            $("#nav-positionning").text("关闭定位");
            localStorage.setItem("positionning", "true");
            map.setPositionning(true)
        }
        else {
            $("#nav-positionning").text("开启定位");
            localStorage.setItem("positionning", "false");
            map.setPositionning(false)
        }
    })

    setInterval(() => {
        updatePeopleLocation();
    }, 10000);
    updatePeopleLocation();

});

function showSignInDialog() {
    dialog.dialog("open");
    $("#dialog-btn-sign-in").click(() => signInOrUp(false))
    $("#dialog-btn-sign-up").click(() => signInOrUp(true))
}
function signInOrUp(signUp: boolean) {
    let btn = signUp ? $("#dialog-btn-sign-up") : $("#dialog-btn-sign-in");
    let user = new User();
    user.name = <string>$("#dialog-username").val();
    user.password = <string>$("#dialog-password").val();
    if (user.name == undefined || user.name.trim() == "") {
        alert("用户名不可为空！");
        return;
    }
    if (user.password == undefined || user.password.trim() == "") {
        alert("密码不可为空！");
        return;
    }
    user.password = <string>Md5.hashStr(user.password)
    btn.text("");
    btn.attr("disabled", "true");
    $("<div/>", { "class": "spinner-border spinner-border-sm", "role": "status" }).appendTo(btn);
    if (signUp) {
        postSignUp(user, (succeed, message, data) => {
            if (succeed) {
                dialog.dialog("close");
                user.token = data!
                User.setCurrent(user);
                setGroupText();
            }
            else {
                alert("注册失败：" + message);
                btn.removeAttr("disabled");
                btn.text(signUp ? "注册" : "登录");
            }
        })

    }
    else {
        postSignIn(user, (succeed, message, data) => {
            if (succeed) {
                dialog.dialog("close");
                User.setCurrent(data!);
                setGroupText();
            }
            else {
                alert("登录失败：" + message);
                btn.removeAttr("disabled");
                btn.text(signUp ? "注册" : "登录");
            }
        })

    }
}
function setGroupText() {
    if (User.getCurrent() == null) {
        $("#nav-current-group").text("当前组：（未登录）");
    }
    else if (User.getCurrent()!.groupName == undefined || User.getCurrent()!.groupName == "") {
        $("#nav-current-group").text("当前组：（空）");
    }
    else {

        $("#nav-current-group").text("当前组：" + User.getCurrent()!.groupName);
    }

}
function updatePeopleLocation(): void {
    if (User.getCurrent() == null) {
        return;
    }
    postGetAll((succeed, message, users) => {
        if (!succeed) {
            console.log("获取失败：" + message);

            return;
        }
        console.log(users);

        let members = $("#nav-group-members");
        members.empty();
        // let locations:Location[]=[]
        for (const user of users!!) {
            $("<a/>", {
                "class": "dropdown-item",
                "href": "#"
            }).text((user.displayName == null ? "" : user.displayName) + "(" + user.name + ")")
                .click(() => {
                    if (user.lastLocation != null) {
                        map.to([user.lastLocation.longitude!, user.lastLocation.latitude!], 15);
                        map.select(user.name);
                    }
                })
                .appendTo(members);
            // if(user.lastLocation!=null)
            // {
            //     locations.push(user.lastLocation)
            // }
        }

        map.update(users!);
    })
}




