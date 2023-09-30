/**
 * Home
 */
export default [
    {
        //task board
        name: 'task',
        path: '/project/space/task/:code',
        component: resolve => require(['@/views/project/space/task'], resolve),
        meta: {model: 122, info: {show_slider: false, is_inner: true}},
        children: [
            {
                //task details
                name: 'taskdetail',
                path: 'detail/:taskCode',
                component: resolve => require(['@/views/project/space/taskdetail'], resolve),
                meta: {model: 'Project', info: {show_slider: false}},
            },
        ]
    },
    {
        //invite link
        name: 'inviteFromLink',
        path: '/invite_from_link/:code',
        component: resolve => require(['@/views/common/inviteFromLink'], resolve),
        meta: {model: 'Common', info: {show_slider: false}},
    },
];
