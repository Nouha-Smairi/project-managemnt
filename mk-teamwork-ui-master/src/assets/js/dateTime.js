/**
 *Get this Monday's time
 * @returns {number}
 */
import moment from "moment";

export const getWeekDay = () => {
    //array of start and end dates
    var startStop = [];
    //get current time
    var currentDate = new Date();
    //return date is a day of the week
    var week = currentDate.getDay();
    //return date is a day of the month
    var month = currentDate.getDate();

    //milliseconds of a day
    var millisecond = 1000 * 60 * 60 * 24;
    //subtracted days
    var minusDay = week != 0 ? week - 1 : 6;
    //alert(minusDay)
    //this week monday
    var monday = new Date(currentDate.getTime() - (minusDay * millisecond));
    //this week sunday
    var sunday = new Date(monday.getTime() + (6 * millisecond));
    monday = monday.getFullYear() + "-" + (monday.getMonth() + 1) + "-" + monday.getDate();
    sunday = sunday.getFullYear() + "-" + (sunday.getMonth() + 1) + "-" + sunday.getDate();
    var week_day = {
        monday: monday,
        sunday: sunday,
    };
    return week_day
};
/**
 *format relative time
 * @returns {string}
 * @param value
 * @param now
 */
export const relativelyTime = (value, now) => {
    if (!now) {
        now = moment();
    }
    const diff = moment(now).diff(moment(value), 'hours');
    if (diff <= 1) {
        return moment(value).fromNow();
    }
    const today = moment(now).get('date');
    const current = moment(value).get('date');
    if (current < today - 1) {
        // return moment(value).format('M月D日 H:mm');
    }
    let detailTime = moment(value).format('H:mm');
    const currentDate = moment(value).format('YYYY-MM-DD');
    const currentMonday = moment().weekday(0).format('YYYY-MM-DD');
    const currentSunday = moment().weekday(6).format('YYYY-MM-DD');
    let weekDate = '';
    if (currentDate < currentMonday) {
        weekDate = '[last week]dd ' + detailTime;
    }else if (currentDate > currentSunday) {
        weekDate = '[next week] dd' + detailTime;
    }else{
        weekDate = '[week]dd ' + detailTime;
    }
    return moment(value).calendar(null, {
        sameDay: '[today]' + moment(value).format('H:mm'),
        nextDay: '[tomorrow]' + detailTime,
        nextWeek: weekDate,
        lastDay: '[yesterday ]' + detailTime,
        lastWeek: weekDate,
        sameElse: 'M month D H:mm'
    });
};
/**
 *
 * @param value date
 * @param showDetailTime Whether to display the specific time
 * @returns {string}
 */
export const relativelyTaskTime = (value, showDetailTime = false) => {
    let detailTime = moment(value).format('H:mm');
    if (!showDetailTime) {
        detailTime = '';
    }
    const currentDate = moment(value).format('YYYY-MM-DD');
    const currentMonday = moment().weekday(0).format('YYYY-MM-DD');
    const currentSunday = moment().weekday(6).format('YYYY-MM-DD');
    let weekDate = '';
    if (currentDate < currentMonday) {
        weekDate = '[last week]dd ' + detailTime;
    }else if (currentDate > currentSunday) {
        weekDate = '[next week]dd ' + detailTime;
    }else{
        weekDate = '[week]dd ' + detailTime;
    }
    return moment(value).calendar(null, {
        sameDay: '[today ]' + moment(value).format('H:mm'),
        nextDay: '[tomorrow ]' + detailTime,
        nextWeek: weekDate,
        lastDay: '[yesterday]' + detailTime,
        lastWeek: weekDate,
        sameElse: 'M month D H:mm'
    });
};
export const formatTaskTime = (begin, end) => {
    //If it is not today, the specific time will not be displayed
    if (!end && begin) {
        return relativelyTaskTime(begin) + ' start';
    }
    if (!begin) {
        return relativelyTaskTime(end) + ' cut off';
    }
    return relativelyTaskTime(begin) + ' - ' + relativelyTaskTime(end);
};
/**
 * format time
 * @param data
 * @param show
 * @returns {string}
 */
export const format_date = (data, show) => {
    if (show == undefined) {
        show = true
    }
    //format time
    let now = new Date(data * 1000);
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

export const formatDateNow = (day) => {
    //format time
    if (day == undefined) {
        day = 0;
    }
    let now = new Date();
    let year = now.getFullYear();
    let month = now.getMonth() + 1;
    let date = now.getDate() + day;
    if (month < 10) {
        month = '0' + month;
    }
    if (date < 10) {
        date = '0' + date;
    }
    return year + "-" + month + "-" + date;
};

/**
 * Format project task time
 * @param begin_time
 * @param end_time
 * @returns {string}
 */
export const showTaskTime = (begin_time, end_time) => {
    let task_time = '';
    let begin_time_format = '';
    let end_time_format = '';
    begin_time = Date.parse(new Date(begin_time)) / 1000;
    end_time = Date.parse(new Date(end_time)) / 1000;
    if (begin_time > 0) {
        let begin = format_date(begin_time, false);
        begin_time_format = begin.month + 'month' + begin.day + 'day' + ' - '
    }
    if (end_time > 0) {
        let end = format_date(end_time, false);
        end_time_format = end.month + 'month' + end.day + 'day';
        if (end.hour > 12 && end.hour <= 18) {
            end_time_format += ' afternoon'
        }
        if (end.hour > 18) {
            end_time_format += 'work overtime'
        }
        if (end.hour <= 12 && end.hour >= 8) {
            end_time_format += ' morning'
        }
        if (end.hour < 8 && end.hour > 0) {
            end_time_format += ' overnight'
        }
    }
    if (begin_time_format == '') {
        end_time_format += 'Finish'
    }
    task_time += begin_time_format + end_time_format;
    return task_time
};
/**
 *
 * @returns {string}
 * @param time
 */
export const showHelloTime = (time) => {
    let time_format = '';
    if (time == undefined) {
        time = new Date();
    }
    let hr = time.getHours();
    if ((hr >= 0) && (hr <= 4))
        time_format = "late at night，take care,";
    if ((hr >= 4) && (hr < 7))
        time_format = "good morning， ";
    if ((hr >= 7) && (hr < 12))
        time_format = "Good morning,";
    if ((hr >= 12) && (hr <= 13))
        time_format = "It's time for lunch,";
    if ((hr >= 13) && (hr <= 17))
        time_format = "good afternoon,";
    if ((hr >= 17) && (hr <= 18))
        time_format = "It's evening,";
    if ((hr >= 18) && (hr <= 20))
        time_format = "Eaten dinner yet,";
    if ((hr >= 20) && (hr <= 24))
        time_format = "Are you working overtime? Thanks for your hard work,";
    return time_format
};

/**
 * formatted date object
 * @returns {string}
 * @param date
 * @param format
 */
export const dateFormat = (date, format) => {
    let o = {
        "M+": date.getMonth() + 1, //month
        "d+": date.getDate(),    //day
        "h+": date.getHours(),   //hour
        "m+": date.getMinutes(), //minute
        "s+": date.getSeconds(), //second
        "q+": Math.floor((date.getMonth() + 3) / 3),  //quarter
        "S": date.getMilliseconds() //millisecond
    };
    if (/(y+)/.test(format)) {
        format = format.replace(RegExp.$1,
            (date.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for (let k in o) if (new RegExp("(" + k + ")").test(format))
        format = format.replace(RegExp.$1,
            RegExp.$1.length == 1 ? o[k] :
                ("00" + o[k]).substr(("" + o[k]).length));
    return format;
};

/**
 * Numbers to English letters
 *like：1->A
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
