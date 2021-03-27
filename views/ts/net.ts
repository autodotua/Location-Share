
import $ from "jquery";
import { Location as getGroupId, Response, User, Location } from "./common";

// let rootUrl = "http://192.168.2.10:8080/api/";
let rootUrl="api/";
// let rootUrl="http://locshare.autodotua.top/api/";


export function postSignIn(user: User, callback?: (succeed: boolean,message:string, data?: User) => void) {
    post("signIn", user, callback)
}
export function postSignUp(user: User, callback?: (succeed: boolean,message:string, data?: string) => void) {
    post("signUp", user, callback)
}
export function postCheckToken(callback?: (succeed: boolean,message:string, data?: any) => void) {
    post("checkToken", null, callback)
}
export function postUpdate(location: Location, callback?: (succeed: boolean,message:string, data?: any) => void) {
    post("update", location, callback)
    console.log("上传位置");
}
export function postGetAll(callback?: (succeed: boolean,message:string, data?: User[]) => void) {
    post("get", { time: 60 * 30 }, callback)
    console.log("获取");
    
}
export function postUserInfo(user: User, callback?: (succeed: boolean,message:string, data?: any) => void) {
    post("userInfo", user, callback)
}

function post<T>(subUrl: string, data: any, callback?: (succeed: boolean,message:string, data?: T) => void) {
    $.post({
        url: rootUrl + subUrl,
        contentType: "application/json",
        data: JSON.stringify({
            user: User.getCurrent(),
            data: data
        }),
        success: response => {
            if (callback) {
                callback(response.succeed,response.message, response.data );
            }

        },
        error: function (jqXHR, textStatus, errorThrown) {
            if (callback) {
                callback(false,"通信失败（"+textStatus+"）", undefined);
            }
        }
    })
}




function alertAjaxError(msg: string, code: JQuery.jqXHR): void {
    console.log(msg + "：\n" + JSON.stringify(code));
}