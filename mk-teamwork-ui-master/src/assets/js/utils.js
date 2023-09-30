import {notice} from './notice';
import config from '../../config/config'
import {getStore} from "./storage";

const PROD_URL = config.PROD_URL;
const crossDomain = config.crossDomain;

/**
 * Determine the client's return status
 * @param res
 * @param show_msg
 * @returns {boolean}
 */
export const checkResponse = (res, show_msg = false) => {
    const code = res.code;
    const msg = res.msg;
    if (code !== 200) {
        if (show_msg) {
            notice(msg);
        }
        return false
    } else {
        return true
    }
};
/**
 * Create route object
 * @returns {boolean}
 * @param data
 */
export const createRoute = (data) => {
    let path = data.url;
    if (data.params) {
        path += '/' + data.params;
    }
    let filePath = data.url;
    if (data.file_path) {
        filePath = data.file_path;
    }
    return {
        name: data.id,
        path: path,
        component: resolve => require(['@/views/' + filePath], resolve),
        meta: {model: data.pid, info: data},
    };
};

export const getBase64 = (img, callback) => {
    const reader = new FileReader();
    reader.addEventListener('load', () => callback(reader.result));
    reader.readAsDataURL(img);
};

/**
 * Operation Confirmation
 * @param options
 * @param callback
 */
export const showWarConfirm = (options = {}, callback = function () {
}) => {
    Modal.confirm({
        title: options.title || 'Operating Tips',
        content: '<p>' + options.content + '</p>',
        loading: true,
        onOk: () => {
            callback()
        }
    });
};

/**
 * Get the complete api request address
 */
export const getFullUrl = (api) => {
    return PROD_URL + '/' + api
};

export const getApiUrl = (api) => {
    if (crossDomain) {
        return PROD_URL + '/' + api; //Enable cross-domain direct return
    }
    if (process.env.NODE_ENV === 'production') {
        return PROD_URL + '/' + api;
    } else {
        return '/api/' + api;
    }
};

/**
 * Get the URL of the uploaded file
 * @param api
 * @returns {string}
 */
export const getUploadUrl = (api) => {
    let baseUrl = '';
    // if (process.env.NODE_ENV === 'production') {
    //     baseUrl = PROD_URL
    // }
    return baseUrl + getApiUrl(api)
};


export const format_date = (data, show = true) => {
    //format time
    let now = null;
    if (isNaN(data)) {
        now = new Date(data * 1000);

    } else {
        now = new Date(data);
    }
    let year = now.getFullYear();
    let month = now.getMonth() + 1;
    let date = now.getDate();
    let hour = now.getHours();
    let minute = now.getMinutes();
    // let second = now.getSeconds();
    if (month < 10) {
        month = '0' + month;
    }
    if (date < 10) {
        date = '0' + date;
    }
    if (hour < 10) {
        hour = '0' + hour;
    }
    if (minute < 10) {
        minute = '0' + minute;
    }
    const finally_date = {
        year: year,
        month: month,
        day: date,
        hour: hour,
        minute: minute
    };
    if (show) {
        return year + "-" + month + "-" + date + "   " + hour + ":" + minute;
    } else {
        return finally_date
    }
};
/**
 * Convert the time to a readable format, and pass in the time value of the date
 * @param time
 * @returns {*}
 */
export const prettyTime2Chinese = (time) => {
    if (!time) {
        return '';
    }
    if (isNaN(time)) {
        return 'incorrect format';
    }
    var minute = 60 * 1000, //1 minute
        hour = 60 * minute, //1Hour
        day = 24 * hour, //1day
        month = 12 * day,//month
        year = 12 * month;//year

    var diff = new Date().getTime() - time;
    var r = 0;
    if (diff > year) {
        r = parseInt(diff / year);
        return r + "Years ago";
    }
    if (diff > month) {
        r = parseInt(diff / month);
        return r + "month ago";
    }
    if (diff > day) {
        r = parseInt(diff / day);
        if (r == 1) {
            return "yesterday";
        }
        return r + "Days ago";
    }
    if (diff > hour) {
        r = parseInt(diff / hour);
        return r + "hours ago";
    }
    if (diff > minute) {
        r = parseInt(diff / minute);
        return r + "minutes ago";
    }
    return "just recently";
};

/**
 * Numbers to English letters
 * like：1->A
 * @param num
 * @returns {string}
 */
export const convert = (num) => {
    let result = "";
    while (num) {
        result = String.fromCharCode(--num % 26 + 65) + result;
        num = Math.floor(num / 26)
    }
    return result
};

export function timeFix() {
    const time = new Date();
    const hour = time.getHours();
    return hour < 9 ? 'Good morning' : (hour <= 11 ? 'good morning' : (hour <= 13 ? 'good afternoon' : (hour < 20 ? 'good afternoon' : 'Good evening')))
}

export function getAuthorization() {
    let tokenList = getStore('tokenList', true);
    if (tokenList) {
        let accessToken = tokenList.accessToken;
        let tokenType = tokenList.tokenType;
        return {Authorization: `${tokenType} ${accessToken}`};
    }
    return {};
}

/**
 *Get push notifications
 * 如：1->A
 * @param num
 * @returns {string}
 */
export const getPushData = (data) => {
    return JSON.parse(data)
};
// export const snail = (array) => {
//   let arrs = [];
//   for(var i=0,l=array.length;i<l;i++) {
//     if(!Array.isArray(array[i])) arrs.push(array[i]);
//     else arrs = arrs.concat(snail(array[i]) );
//   }
//   return arrs;
// }
/*export const snail = (obj) => {
    for (var a in obj) {
        if (typeof (obj[a]) == "object") {
            return snail(obj[a], value); //递归遍历
        } else {
            if (a === 'path') {
                if (obj[a] === value) {
                    console.log(a + "=" + obj[a]);
                    return true;
                }
            }
        }
    }
};*/

// Determine whether the parameter is one of
export function oneOf(value, validList) {
    for (let i = 0; i < validList.length; i++) {
        if (value === validList[i]) {
            return true;
        }
    }
    return false;
}

/**
 * Get nodes according to class
 * @param className
 * @param tag
 * @returns {NodeList}
 */
export const getClassObj = (className, tag) => {
    tag = tag || document;
    className = className || '*';
    let findarr = [];
    if (document.getElementsByClassName) {
        return document.getElementsByClassName(className)
    }
    let el = document.getElementsByTagName(tag);
    let pattern = new RegExp('(^|\\s)' + className + '(\\s|$)');
    for (let i = 0; i < el.length; i++) {
        if (pattern.test(el[i].className)) {
            findarr.push(el[i])
        }
    }
};

/**
 * Determine whether a dom has a scroll bar
 * @param el dom object
 * @param directionscroll vertically or horizontally
 * @returns {boolean}
 */
export const hasScrolled = (el, direction = "vertical") => {
    if (!el) {
        return false;
    }
    let overflow = el.currentStyle ? el.currentStyle.overflow :
        window.getComputedStyle(el).getPropertyValue("overflow");
    if (overflow === "hidden") return false;

    if (direction === "vertical") {
        return el.scrollHeight > el.clientHeight;
    } else if (direction === "horizontal") {
        return el.scrollWidth > el.clientWidth;
    }
};
/*Judge whether the token has expired*/
export const isTokenExpired = (timeStamp) => {
    let expiredTime = timeStamp;
    /*get local time*/
    let nowTime = new Date().getTime() / 1000;
    /*If < 30 minutes, it means it is about to expire*/
    return (expiredTime - nowTime) < 60 * 30
};

//Implement a method that can traverse multidimensional arrays, then add the method to the prototype
// One of the functions of the prototype is to leave us to extend the properties and methods of the object
//We add an each method to the array to traverse the multidimensional array and pass in a callback function
Array.prototype.each = function (fn) {
    try {  //core business logic
        this.i || (this.i = 0); //Define a counter, if it exists, it is the original, if it does not exist, it is initialized to 0
        //When the array has a length and a function is passed in, we execute the callback on the array
        if (this.length > 0 && fn.constructor === Function) {
            while (this.i < this.length) {    //to traverse
                var e = this[this.i]; //Get the current element
                //If the obtained e element is an array, then recurse until it is an element and then execute the callback
                if (e && e.constructor === Array) {
                    e.each(fn);
                } else {
                    //Enter here to explain that the e element is a single element
                    //We bind the method for the e element, which is equivalent to calling the fn method for e
                    //fn.apply(e,[e]); or
                    fn.call(e, e);
                }
                this.i++;
            }
        }
        this.i = null;    //Do garbage collection Remove reference markers
    } catch (ex) {
        console.log(ex);
        //do something
    }
};
