import Vue from 'vue'

Vue.filter('NumberFormat', function (value) {
    if (!value) {
        return '0'
    }
    const intPartFormat = value.toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,'); //Break the integer part every three
    return intPartFormat
});

//file preview
Vue.filter('showPreviewUrl', function (file) {
    let url = file.file_url;
    const docUrl = 'https://view.officeapps.live.com/op/view.aspx?src=';
    let docArr = ['doc', 'docx', 'docm', 'dotm', 'dotx', 'xlsx', 'xlsb', 'xls', 'xlsm', 'pptx', 'ppsx', 'ppt', 'pps', 'pptm', 'potm', 'ppam', 'potx', 'ppsm'];
    const extension = file.extension;
    const index = docArr.findIndex(item => item == extension);
    if (index !== -1) {
        url = docUrl + url;
    }
    return url;
});
