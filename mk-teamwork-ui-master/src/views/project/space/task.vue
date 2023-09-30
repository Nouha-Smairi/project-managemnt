<template>
    <div class="project-space-task" :class="project.task_board_theme">
        <div class="project-navigation">
            <div class="project-nav-header">
                <a-breadcrumb>
                    <a-breadcrumb-item>
                        <a @click="toHome">
                            <a-icon type="home"/>
                            front page
                        </a>
                    </a-breadcrumb-item>
                    <a-breadcrumb-item>
                        <project-select class="nav-title" style="display: inline-block" :code="code"></project-select>
                        <span class="actions">
                             <a-tooltip :mouseEnterDelay="0.3" :title="project.collected ? 'cancel favorite' : 'add to favorites'"
                                        @click="collectProject">
                            <a-icon type="star" theme="filled" style="color: grey;" v-show="!project.collected"/>
                            <a-icon type="star" theme="filled" style="color: #ffaf38;" v-show="project.collected"/>
                        </a-tooltip>
                        </span>
                        <span class="label label-normal" v-if="project.private === 0"><a-icon type="global"/> public</span>
                    </a-breadcrumb-item>
                </a-breadcrumb>
            </div>
            <section class="nav-body">
                <ul class="nav-wrapper nav nav-underscore pull-left">
                    <li class="actives"><a class="app" data-app="tasks">Task</a></li>
                    <li class=""><a class="app" data-app="works"
                                    @click="$router.push('/project/space/files/' + code)">
                        document</a>
                    <li class=""><a class="app" data-app="build"
                                    @click="$router.push('/project/space/overview/' + code)">
                        overview</a>
                    </li>
                    <li class=""><a class="app" data-app="build"
                                    @click="$router.push('/project/space/features/' + code)">
                        version</a>
                    </li>
                </ul>
            </section>
            <div class="project-nav-footer">
                <a class="footer-item" @click="visibleDraw('taskSearch')">
                    <a-icon type="search"></a-icon>
                    <span>filter</span>
                </a>
                <a class="footer-item" :class="{active:slideMenuKey == 'member'}" @click="visibleDraw('member')">
                    <a-icon type="user"></a-icon>
                    <span> {{projectMembers.length}}</span>
                </a>
                <a class="footer-item" :class="{active:slideMenuKey == 'config'}" @click="visibleDraw('config')">
                    <a-icon type="menu"></a-icon>
                    <span> menu</span>
                </a>
            </div>
        </div>
        <wrapper-content :showHeader="false">
            <draggable v-model="taskStages"
                       :options="{group:'stages',filter:'.undraggables',handle:'.ui-sortable-handle',ghostClass:'stage-ghost',animation: 200,forceFallback:false}"
                       id="board-scrum-stages" class="board-scrum-stages" :move="stageMove" @update="stageSort">
                <div class="scrum-stage" v-for="(stage,index) in taskStages" :key="index" :id="stage.code"
                     :class="{ 'fixed-creator': stage.fixedCreator == true}">
                    <!--<a-spin wrapperClassName="tasks-loading" :spinning="stage.tasksLoading">-->
                    <!--<a-tooltip placement="top" >
                     <template slot="title">
                         {{ stage.name }}
                         <span class="task-count" v-if="stage.list.length > 0"> · {{ stage.list.length }}</span>
                     </template>-->
                    <header class="scrum-stage-header ui-sortable-handle" v-show="!stage.tasksLoading">
                        <div class="stage-name hinted">
                            {{ stage.name }}
                            <span class="task-count"
                                  v-if="stage.tasks.length > 0"> · {{ stage.tasks.length }}</span>
                        </div>
                        <div class="stage-menu-toggler popover">
                            <a-dropdown :trigger="['click']" placement="bottomCenter">
                                <a-tooltip placement="top">
                                    <template slot="title">
                                        <span>Edit task list</span>
                                    </template>
                                    <a href="javascript:void(0)" class="menu-toggler-title">
                                        <a-icon type="ellipsis" style="font-size: 18px;"/>
                                    </a>
                                </a-tooltip>
                                <!--<div slot="overlay" class="task-popover-content">-->
                                <!--<header class="popover-header" name="333">
                                    <span class="popover-title">list menu</span>
                                </header>-->
                                <a-menu slot="overlay" @click="doStage" :selectable="false">
                                    <a-menu-item :key="'editStage_' + stage.code + '_' + index">
                                        <a-icon type="edit"></a-icon>
                                        edit list
                                    </a-menu-item>
                                    <a-menu-item :key="'setExecutor_' + stage.code + '_' + index">
                                        <a-icon size="14" type="user"></a-icon>
                                        Set all task executors in this column
                                    </a-menu-item>
                                    <!--<a-menu-item :key="'setEndTime_' + stage.code + '_' + index">
                                        <a-icon size="14" type="clock-circle"></a-icon>
                                        设置本列所有任务截止时间 *
                                    </a-menu-item>-->
                                    <a-menu-item :key="'recycleBatch_' + stage.code + '_' + index">
                                        <a-icon size="14" type="delete"></a-icon>
                                        Move all tasks in this column to the trash
                                    </a-menu-item>
                                    <a-menu-item :key="'delStage_' + stage.code + '_' + index">
                                        <a-icon size="14" type="delete"></a-icon>
                                        delete list
                                    </a-menu-item>
                                </a-menu>
                                <!--</div>-->
                            </a-dropdown>
                        </div>
                    </header>
                    <!--</a-tooltip>-->
                    <div class="scrum-stage-wrap ui-sortable"
                         :class="{ 'hidden-creator-bottom': stage.showTaskCard}">
                        <vue-scroll :ref="index + '-stage'" @handle-resize="handleResize($event,index)"
                                    :ops="scrollOps">
                            <section :id="stage.code" :task-type-index="index"
                                     class="scrum-stage-content thin-scroll">
                                <a-spin wrapperClassName="tasks-loading" :spinning="stage.tasksLoading">
                                    <!--unfinished list-->
                                    <draggable v-model="stage.unDoneTasks"
                                               :options="{group:'task',ghostClass:'task-ghost',dragClass:'task-drag',fallbackClass:'task-drag',forceFallback:false}"
                                               @end="taskSort"
                                               class="scrum-stage-tasks">
                                        <template v-for="(task,taskIndex) in stage.unDoneTasks">
                                            <div class="task task-card ui-sortable-handle"
                                                 :index="taskIndex"
                                                 :id="task.code"
                                                 :key="task. code"
                                                 :class="showTaskPri(task.pri)"
                                                 v-if="!task.done && task.canRead"
                                                 @click.stop="taskDetail(task.code,index)"
                                            >
                                                <div class="task-priority bg-priority-0"></div>
                                                <a-tooltip :placement="index > 0 ? 'top':'right'">
                                                    <template slot="title">
                                                        <span v-if="task.hasUnDone" style="font-size: 12px;">The subtasks have not all been completed and the parent task cannot be completed</span>
                                                    </template>
                                                    <div class="check-box-wrapper"
                                                         @click.stop="taskDone(task.code,index,taskIndex,1)">
                                                        <a-icon class="check-box" :class="{'disabled': task.hasUnDone}"
                                                                type="border" :style="{fontSize:'16px'}"/>
                                                    </div>
                                                    <!--<a class="check-box"
                                                       :class="{'disabled': task.hasUnDone}"
                                                       @click.stop="taskDone(task.code,index,taskIndex,1)"></a>-->
                                                </a-tooltip>
                                                <div class="task-content-set open-detail">
                                                    <div class="task-content-wrapper">
                                                        <div class="task-content"> {{ task.name }}</div>
                                                        <a-tooltip placement="top"
                                                                   v-if="task.executor && task.executor.avatar">
                                                            <template slot="title">
                                                                <span>{{task.executor.name}}</span>
                                                            </template>
                                                            <img
                                                                    :src="task.executor.avatar"
                                                                    :title="task.executor.name"
                                                                    class="avatar img-circle img-24 hinted">
                                                        </a-tooltip>
                                                    </div>
                                                    <div class="task-info-wrapper clearfix">
                                                        <div class="task-infos">
                                                            <span class="icon-wrapper" :style="{color: getStatusColor(task.status), fontSize: '12px'}" v-if="task.status">{{task.statusText}}</span>
                                                       <span class="label" :class="showTimeLabel(task.end_time)"
                                                             v-if="task.end_time">
                                                            <span :title="task.end_time">
                                                               {{ showTaskTime(task.begin_time,task.end_time)}}
                                                            </span>
                                                        </span>
                                                            <span class="icon-wrapper muted" v-if="task.description">
                                                           <a-icon type="file-text"></a-icon>
                                                       </span>
                                                            <span class="icon-wrapper muted" v-if="task.hasSource">
                                                             <a-icon type="link"></a-icon>
                                                       </span>
                                                            <span class="icon-wrapper muted" v-if="task.hasComment">
                                                           <a-icon type="message"/>
                                                        </span>
                                                            <span class="icon-wrapper muted"
                                                                  v-if="task.childCount[0] > 0">
                                                             <a-icon type="bars"></a-icon>
                                                            <span>{{task.childCount[1]}}/{{task.childCount[0]}}</span>
                                                       </span>
                                                            <span class="tag muted" v-for="tag in task.tags"
                                                                  :key="tag.code"
                                                            >
                                                                <a-badge status="success"
                                                                         :class="`badge-${tag.tag.color}`"/>
                                                               {{tag.tag.name}}
                                                           </span>
                                                            <span :class="'icon-wrapper text text-' + task.task_execute.color"
                                                                  v-if="task.execute_state > 0">{{ task.task_execute_name }}</span>
                                                            <span class="icon-wrapper muted" v-if="task.like">
                                                           <a-icon type="like"/>
                                                                <span>{{task.like}}</span>
                                                       </span>
                                                        </div>
                                                    </div>
                                                    <footer class="task-info-footer"
                                                            v-if="project.prefix && project.open_prefix">
                                                        <span class="task-id-number">
                                                            {{project.prefix}}-{{task.id_num}}
                                                        </span>
                                                    </footer>
                                                </div>
                                            </div>
                                        </template>
                                    </draggable>
                                    <!--Create task cards-->
                                    <div class="task-creator-wrap card" :id="'card' + index"
                                         v-show="stage.showTaskCard">
                                        <form class="task-creator">
                                            <a-input :ref="`inputTaskName${index}`" v-model="task.name"
                                                     class="task-content-input" type="textarea"
                                                     :rows="3"
                                                     placeholder="task content" @keyup.enter="createTask(stage.code,index)"/>
                                            <div class="handler-wrap">
                                                <a-dropdown :trigger="['click']">
                                                    <a class="executor-handler"><img
                                                            :src="defaultExecutor.avatar"
                                                            class="avatar img-circle img-24 hinted"> <span
                                                            class="creator-handler-text name">{{ defaultExecutor.name }}</span></a>
                                                    <a-menu class="executor-handler-menu" @click="selectExecutor"
                                                            slot="overlay">
                                                        <a-menu-item
                                                                v-for="(member,index) in projectMembers"
                                                                :key="index">
                                                            <img class="avatar img-circle img-32 pull-left m-r-sm "
                                                                 :src="member.avatar">
                                                            <span class="muted"
                                                                  style="line-height: 25px;">{{ member.name }}</span>
                                                            <a-icon type="check" class="muted"
                                                                    v-if="member.code == defaultExecutor.code"></a-icon>
                                                        </a-menu-item>
                                                    </a-menu>
                                                </a-dropdown>
                                            </div>
                                            <div class="submit-set">
                                                <a-button type="default" size="large" class="middle-btn"
                                                          @click.stop="showTaskCard(index,false)">Cancel
                                                </a-button>
                                                <a-button :loading="createTaskLoading" :disabled="!task.name"
                                                          type="primary" size="large"
                                                          class="middle-btn" :class="{'disabled-btn':!task.name}"
                                                          @click.stop="createTask(stage.code,index)">create
                                                </a-button>
                                            </div>
                                        </form>
                                    </div>
                                    <!--completed list-->
                                    <draggable v-model="stage.doneTasks"
                                               :options="{group:'task-done',ghostClass:'task-ghost',dragClass:'task-drag',fallbackClass:'task-drag',forceFallback:false}"
                                               @end="taskSort"
                                               class="scrum-stage-tasks-done">
                                        <!--<ul v-if="stage.tasks.length" class="scrum-stage-tasks-done">-->
                                        <template v-for="(task,taskIndex) in stage.doneTasks">
                                            <div class="task done task-card ui-sortable-handle"
                                                 :index="taskIndex"
                                                 :id="task.code"
                                                 :key="task.code"
                                                 v-if="task.canRead"
                                                 @click.stop="taskDetail(task.code,index)"
                                            >
                                                <div class="task-priority bg-priority-0"></div>
                                                <span class="check-box-wrapper"
                                                      @click.stop="taskDone(task.code,index,taskIndex,0)">
                                                       <a-icon class="check-box" type="check-square"
                                                               :style="{fontSize:'16px'}"
                                                               :class="{'disabled': task.hasUnDone}"/>
                                                </span>
                                                <!--<a class="check-box"
                                                   @click.stop="taskDone(task.code,index,taskIndex,0)">
                                                    <a-icon type="check"/>
                                                </a>-->
                                                <div class="task-content-set open-detail">
                                                    <div class="task-content-wrapper">
                                                        <div class="task-content">{{ task.name }}</div>
                                                        <a-tooltip placement="top"
                                                                   v-if="task.executor && task.executor.avatar">
                                                            <template slot="title">
                                                                <span>{{task.executor.name}}</span>
                                                            </template>
                                                            <img v-if="task.executor && task.executor.avatar"
                                                                 :src="task.executor.avatar"
                                                                 :title="task.executor.name"
                                                                 class="avatar img-circle img-24 hinted">
                                                        </a-tooltip>
                                                    </div>
                                                    <div class="task-info-wrapper clearfix">
                                                        <div class="task-infos">
                                            <span class="tag muted" :class="'tag-color-'+ tag.color"
                                                  v-for="(tag,tag_index) in task.task_tag_item_list" :key="tag.code"> {{ tag.name }} </span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                        <!--</ul>-->
                                    </draggable>
                                    <div class="scrum-stage-tasks-done" v-show="stage.canNotReadCount">
                                        <li class="task muted" style="margin: 0 10px 8px;">
                                            <span><a-icon type="lock"></a-icon>
                                            There are {{stage.canNotReadCount}} tasks hidden (because only participants are set)</span>
                                        </li>
                                    </div>
                                    <!--add task button-->
                                    <div class="task-creator-handler-wrap" @click.stop="showTaskCard(index)"
                                         v-if="!stage.showTaskCard">
                                        <a class="task-creator-handler link-add-handler">
                                            <a-icon type="plus-circle" style="padding-right: 6px;"></a-icon>
                                            add task
                                        </a>
                                    </div>
                                </a-spin>
                            </section>
                        </vue-scroll>
                    </div>
                    <!--</a-spin>-->
                </div>
                <div class="scrum-stage undraggable create-stage">
                    <header class="scrum-stage-header">
                        <div class="stage-name hinted" style="width: 100%">
                            <a class="muted" v-show="!showCreateStage" @click="showInputStrageName">
                                <a-icon type="plus"/>
                                <span class="m-l-xs">New task list</span>
                            </a>
                            <div v-show="showCreateStage">
                                <div>
                                    <a-input ref="inputStageName" v-model="stageName" placeholder="New Task List..."
                                             @keyup.enter="creteStage"
                                             auto-focus></a-input>
                                </div>
                                <div class="submit-set create-stage-footer">
                                    <a type="text" class="cancel-text muted"
                                       @click="showCreateStage = !showCreateStage">
                                       Cancel
                                    </a>
                                    <a-button type="primary" class="middle-btn" @click="creteStage">save</a-button>
                                </div>
                            </div>
                        </div>
                    </header>
                </div>
            </draggable>
            <router-view></router-view>
        </wrapper-content>
        <!--Edit task list-->
        <a-modal
                :width="360"
                v-model="stageModal.modalStatus"
                :title="stageModal.modalTitle"
                :bodyStyle="{paddingBottom:'1px'}"
                :footer="null"
        >
            <a-form
                    @submit.prevent="editStage"
                    :form="stageModal.form"
            >
                <a-form-item
                >
                    <a-input ref="inputStageTitle" placeholder='list title'
                             v-decorator="[
                                            'name',
                                            {rules: [{ required: true, message: 'Please enter a list title' }]}
                                            ]"/>
                </a-form-item>
                <a-form-item
                >
                    <div class="action-btn pull-right">
                        <a type="text" class="cancel-text muted"
                           @click="stageModal.modalStatus = false">
                           Cancel
                        </a>
                        <a-button type="primary" htmlType='submit' class="middle-btn">save</a-button>
                    </div>
                </a-form-item>
            </a-form>
        </a-modal>
        <!--project member-->
        <a-drawer
                wrapClassName="info-drawer"
                title="project member"
                width=350
                placement="right"
                @close="inviteMemberDraw.visible = false"
                :visible="inviteMemberDraw.visible"
        >
            <div class="search-content">
                <a-input v-model="inviteMemberDraw.keyword" size="large" placeholder="Enter a nickname or email address to find">
                    <a-icon slot="prefix" type="search"/>
                </a-input>
            </div>
            <div class="member-list">
                <div class="member-list-item ant-list-item header-action">
                    <div class="ant-list-item-meta" @click="showInviteMember = true">
                        <div class="ant-list-item-meta-avatar">
                            <a-avatar icon="plus"></a-avatar>
                        </div>
                        <div class="ant-list-item-meta-content">
                            <h4 class="ant-list-item-meta-title"><span>invite new members</span></h4>
                        </div>
                    </div>
                </div>
                <a-list
                        itemLayout="horizontal"
                        :loading="inviteMemberDraw.searching"
                        :dataSource="projectMembers"
                        :locale="{emptyText: (inviteMemberDraw.keyword && inviteMemberDraw.keyword.length > 1) ? '没有搜索到相关成员' : ''}"
                >
                    <a-list-item class="member-list-item" slot="renderItem" slot-scope="item,index">
                   <span slot="actions" v-if="!item.is_owner">
                         <a class="muted" @click="removeMember(item,index)"><a-icon type="user-delete"/> remove</a>
                       <!-- <a-button size="small" type="dashed" icon="user-add"
                                  v-if="!item.is_owner"
                        >operate</a-button>-->
                     </span>
                        <a-list-item-meta
                                :description="item.email"
                        >
                            <span slot="title">{{item.name}}</span>
                            <a-avatar slot="avatar" icon="user" :src="item.avatar"/>
                        </a-list-item-meta>
                    </a-list-item>
                </a-list>
            </div>
        </a-drawer>
        <!--Project settings menu-->
        <a-drawer
                wrapClassName="info-drawer"
                title="project settings"
                width=350
                placement="right"
                @close="configDraw.visible = false"
                :visible="configDraw.visible"
        >
            <div class="config-wrapper">
                <ul class="config-menus">
                    <li class="menu-item">
                        <a @click="projectModal.modalStatus = true">
                            <a-icon type="setting"/>
                            project settings
                        </a>
                    </li>
                    <li class="menu-item">
                        <a @click="taskTagModal.modalStatus = true">
                            <a-icon type="tags"/>
                            Label
                        </a>
                    </li>
                    <li class="menu-item">
                        <a @click="recycleModal.modalStatus = true">
                            <a-icon type="delete"/>
                            View Trash
                        </a>
                    </li>
                    <li class="menu-item">
                        <a :href="downLoadUrl" target="_blank">
                            <a-icon type="copy"/>
                            Download the import task template
                        </a>
                    </li>
                    <li class="menu-item">
                        <a-upload name="file"
                                  :showUploadList="false"
                                  :action="uploadAction"
                                  :beforeUpload="beforeUpload"
                                  :data="{projectCode: code}"
                                  :headers="headers"
                                  @change="handleChange">
                            <a class="text-default" :loading="uploadLoading" :disabled="uploadLoading">
                                <a-icon type="upload" v-show="!uploadLoading"/>
                                Upload file import task
                            </a>
                        </a-upload>
                    </li>
                    <li class="menu-item">
                        <a>
                            <a-icon type="logout"/>
                            export task *
                        </a>
                    </li>
                    <li class="menu-item">
                        <a>
                            <a-icon type="copy"/>
                            copy item *
                        </a>
                    </li>
                    <li class="menu-item">
                        <a>
                            <a-icon type="block"/>
                            Save as project template *
                        </a>
                    </li>
                </ul>

            </div>
        </a-drawer>
        <!--project settings-->
        <a-modal
                destroyOnClose
                class="project-config-modal"
                :width="800"
                v-model="projectModal.modalStatus"
                :title="projectModal.modalTitle"
                :footer="null"
        >
            <project-config :code="code" @update="updateProject"></project-config>
        </a-modal>
        <!--recycle bin-->
        <a-modal
                class="recycle-bin-modal"
                :width="800"
                v-model="recycleModal.modalStatus"
                :title="recycleModal.modalTitle"
                :footer="null"
        >
            <recycle-bin v-if="recycleModal.modalStatus" :code="code" @update="init"></recycle-bin>
        </a-modal>
        <!--task label-->
        <a-modal
                class="task-tag-modal"
                :width="800"
                v-model="taskTagModal.modalStatus"
                :title="taskTagModal.modalTitle"
                :footer="null"
        >
            <task-tag v-if="taskTagModal.modalStatus" :code="code" @update="init"></task-tag>
        </a-modal>
        <!--Set task executor-->
        <a-modal
                class="invite-project-member"
                :width="360"
                v-model="projectMemberModal.modalStatus"
                :title="projectMemberModal.modalTitle"
                :footer="null"
        >
            <div class="member-list">
                <a-list
                        class="project-list"
                        itemLayout="horizontal"
                        :loading="loading"
                        :dataSource="projectMembers"
                >
                    <a-list-item slot="renderItem" slot-scope="item">
                    <span slot="actions">
                        <a-button size="small" type="dashed" icon="user-add"
                                  @click="setExecutor(item)"
                        >set up</a-button>
                     </span>
                        <a-list-item-meta
                                :description="item.email"
                        >
                            <span slot="title">{{item.name}}</span>
                            <a-avatar slot="avatar" icon="user" :src="item.avatar"/>
                        </a-list-item-meta>
                    </a-list-item>
                </a-list>
            </div>
        </a-modal>

        <a-drawer
                wrapClassName="info-drawer task-search"
                title="task screening"
                width=350
                placement="right"
                @close="taskSearch.visible = false"
                :visible="taskSearch.visible"
        >
            <task-search :project-code="code" @search="taskSearchAction"></task-search>
        </a-drawer>
        <invite-project-member v-model="showInviteMember" :project-code="code"
                               v-if="showInviteMember"></invite-project-member>
    </div>
</template>

<script>
    import {mapState} from 'vuex'
    import _ from 'lodash'
    import moment from 'moment'
    import {COMMON} from '../../../const/common'
    import draggable from 'vuedraggable'
    import projectSelect from '@/components/project/projectSelect'
    import inviteProjectMember from '@/components/project/inviteProjectMember'
    import projectConfig from '@/components/project/projectConfig'
    import RecycleBin from '@/components/project/recycleBin'
    import TaskTag from '@/components/project/taskTag'
    import TaskSearch from '@/components/project/taskSearch'

    import {list as getTaskStages, sort, tasks as getTasks} from "../../../api/taskStages";
    import {read as getProject} from "../../../api/project";
    import {inviteMember, list as getProjectMembers, removeMember} from "../../../api/projectMember";
    import {save as createTask, taskDone, sort as sortTask, recycleBatch, batchAssignTask} from "../../../api/task";
    import {save as createState, edit as editStage, del as delStage} from "../../../api/taskStages";
    import {checkResponse, getApiUrl, getAuthorization, getUploadUrl} from "../../../assets/js/utils";
    import {formatTaskTime} from "../../../assets/js/dateTime";
    import {collect} from "../../../api/projectCollect";
    import {notice} from "../../../assets/js/notice";

    export default {
        name: "project-space-task",
        components: {
            RecycleBin,
            TaskTag,
            draggable,
            projectSelect,
            TaskSearch,
            inviteProjectMember,
            projectConfig
        },
        data() {
            return {
                code: this.$route.params.code,
                loading: true,
                project: {task_board_theme: 'simple'},
                stageName: '',
                task: {}, //current task
                taskStatusList: COMMON.TASK_STATUS,
                taskStages: [], //task list
                defaultExecutor: {},//default executor
                projectMembers: [], //List of project members
                projectMembersCopy: [], //Copy of project member list
                createTaskLoading: false, //Create task loading
                showCreateStage: false,

                preCode: '',
                nextCode: '',

                taskSearchParams: {},

                stageKeys: [],
                stageModal: {
                    form: this.$form.createForm(this),
                    stageCode: '',
                    stageIndex: 0,
                    modalStatus: false,
                    confirmLoading: false,
                    modalTitle: 'edit list',
                },

                slideMenuKey: '',
                showInviteMember: false,
                inviteMemberDraw: {
                    visible: false,
                    keyword: '',
                    searching: false,
                    list: [],
                },
                configDraw: {
                    visible: false,
                },
                taskSearch: {
                    visible: false,
                },


                downLoadUrl: getUploadUrl('project/task/_downloadTemplate'),
                uploadLoading: false,
                uploadAction: getApiUrl('project/task/uploadFile'),

                /*project settings*/
                projectModal: {
                    modalStatus: false,
                    modalTitle: 'project settings',
                },
                /*recycle bin*/
                recycleModal: {
                    modalStatus: false,
                    modalTitle: 'View Trash',
                },
                /*task label*/
                taskTagModal: {
                    modalStatus: false,
                    modalTitle: 'task label',
                },

                /*project member*/
                projectMemberModal: {
                    loading: false,
                    currentStageIndex: 0,
                    modalStatus: false,
                    modalTitle: 'Set task executor',
                },
            }
        },
        computed: {
            ...mapState({
                userInfo: state => state.userInfo,
                viewRefresh: state => state.common.viewRefresh,
                socketAction: state => state.socketAction,
            }),
            headers() {
                return getAuthorization();
            },
            scrollOps() {
                return {
                    rail: {
                        background: '#e5e5e5',
                        opacity: 1
                    },
                    bar: {
                        keepShow: true
                    }
                }
            }
        },
        watch: {
            $route(to, from) {
                if (this.code != to.params.code) {
                    this.code = to.params.code;
                    this.defaultExecutor = this.userInfo;
                    this.getProject();
                    this.getProjectMembers();
                    this.init();
                }
                if (from.name == 'taskdetail') {
                    const stageIndex = from.query.from;
                    this.getTaskStages(false);
                    if (stageIndex != undefined) {
                        let searchParams = this.taskSearchParams;
                        let params = Object.assign({stageCode:this.taskStages[stageIndex].code}, searchParams);
                        getTasks(params).then((res) => {
                            this.taskStages[stageIndex].tasksLoading = false;
                            this.taskStages[stageIndex].tasks = res.data;
                            let doneTasks = this.taskStages[stageIndex].doneTasks = [];
                            let unDoneTasks = this.taskStages[stageIndex].unDoneTasks = [];
                            res.data.forEach((task) => {
                                if (task.done) {
                                    doneTasks.push(task);
                                } else {
                                    unDoneTasks.push(task);
                                }
                            });
                        });
                    }
                }
            },
            socketAction(val) {
                if (val.action === 'organization:task') {
                    const data = val.data.data;
                    if (data.projectCode == this.code) {
                        this.getTaskStages(false);
                    }
                }
            },
            viewRefresh() {
                console.log('viewRefresh');
                // this.getTaskStages(false);
            },
            inviteMemberDraw: {
                handler(newVal) {
                    if (newVal.visible) {
                        this.slideMenuKey = 'member';
                    } else {
                        this.slideMenuKey = false;
                    }
                    if (newVal.keyword) {
                        this.searchInviteMember();
                    }
                },
                deep: true
            },
            configDraw: {
                handler(newVal) {
                    if (newVal.visible) {
                        this.slideMenuKey = 'config';
                    } else {
                        this.slideMenuKey = false;
                    }
                },
                deep: true
            },
            showInviteMember(val) {
                if (!val) {
                    this.getProjectMembers();
                }
            },
        },
        created() {
            this.defaultExecutor = this.userInfo;
            this.getProject();
            this.getProjectMembers();
            this.init();
        },
        methods: {
            init() {
                this.getTaskStages();
            },
            getProject() {
                this.loading = true;
                getProject(this.code).then((res) => {
                    this.loading = false;
                    this.project = res.data;
                });
            },
            getProjectMembers() {
                getProjectMembers({projectCode: this.code, pageSize: 100}).then((res) => {
                    this.projectMembers = res.data.list;
                    this.projectMembersCopy = res.data.list;
                });
            },
            getTaskStages(showLoading = true) {
                let app = this;
                getTaskStages({projectCode: this.code, pageSize: 30}).then((res) => {
                    let taskStages = [];
                    if (!showLoading) {
                        res.data.list.forEach((v) => {
                            v.tasksLoading = false;
                            taskStages.push(v);
                        });
                        // this.taskStages = taskStages;
                    } else {
                        //Early assignment, showing loading
                        this.taskStages = taskStages = res.data.list;
                    }
                    if (taskStages) {
                        let searchParams = app.taskSearchParams;
                        let params = {};
                        taskStages.forEach((v, k) => {
                            params = {stageCode: v.code};
                            params = Object.assign(params, searchParams);
                            getTasks(params).then((res) => {
                                let canNotReadCount = 0;
                                res.data.forEach((task) => {
                                    if (!task.canRead) {
                                        canNotReadCount++;
                                    }
                                    if (task.done) {
                                        v.doneTasks.push(task);
                                    } else {
                                        v.unDoneTasks.push(task);
                                    }
                                });
                                v.canNotReadCount = canNotReadCount;
                                v.tasksLoading = false;
                                v.tasks = res.data;
                                if (!showLoading) {
                                    app.$set(app.taskStages, k, v);
                                }
                            })
                        });
                    }
                })
            },
            filterTask(tasks, done) {
                return tasks.filter(item => item.done == done);
            },
            taskSearchAction(value) {
                console.log(value);
                this.taskSearchParams = value;
                this.getTaskStages();
            },
            //Show add task card
            showTaskCard(index = false, show = true) {
                this.taskStages.forEach((v) => {
                    v.showTaskCard = false;
                });
                if (index === false) {
                    return false;
                }
                this.taskStages[index].showTaskCard = show;
                this.$nextTick(() => {
                    //Scroll create to create window
                    this.$refs[index + '-stage'][0].scrollIntoView('#card' + index);
                    this.$refs[`inputTaskName${index}`][0].focus();

                });
            },
            selectExecutor({key}) {
                this.defaultExecutor = this.projectMembers[key];
            },
            //Ready to add tasks
            createTask(stageCode, stageIndex) {
                if (!this.task.name) {
                    this.$message.warning('Task content cannot be empty', 2);
                    return false
                }
                this.task.stage_code = stageCode;
                this.task.project_code = this.code;
                this.task.assign_to = this.defaultExecutor.code;
                //Judging line breaks and adding multiple tasks
                // let titles = this.task.name.split("\n");
                // if (titles.length > 1) {
                //     this.$confirm({
                //         title: 'task prompt',
                //         content: `The system detects that you have entered the ${titles.length} line. Do you want to create multiple tasks?`,
                //         okText: 'Create 1 article',
                //         cancelText: `Create ${titles.length} bars`,
                //         onCancel() {
                //             console.log('Cancel');
                //         },
                //         onOk() {
                //             this.confirmCreateTask(stageIndex);
                //             return Promise.resolve();
                //         }
                //     });
                //     return false;
                // }
                this.confirmCreateTask(stageIndex);
            },
            //add task
            confirmCreateTask(stageIndex) {
                let app = this;
                if (app.createTaskLoading) {
                    app.$message.warning('Adding task, please wait...', 2);
                    return false;
                }
                setTimeout(function () {
                    if (app.createTaskLoading === true) {
                        app.$message.loading({
                            content: 'Adding task, please wait...',
                            duration: 5
                        })
                    }
                }, 2000);
                app.createTaskLoading = true;
                createTask(app.task).then(res => {
                    app.createTaskLoading = false;
                    const result = checkResponse(res);
                    if (result) {
                        app.$message.destroy();
                        let taskStages = app.taskStages[stageIndex];
                        taskStages.tasks.push(res.data);
                        app.taskStages[stageIndex].unDoneTasks.push(res.data);
                        // getTasks({stageCode: app.task.stage_code}).then((res) => {
                        //     let taskStages = app.taskStages[stageIndex];
                        //     taskStages.tasks = res.data;
                        // });
                        app.task = {};
                        // notice({
                        //     title: 'Add task successfully',
                        //     msg: 'You can click on the task to continue with detailed settings'
                        // }, 'notice', 'success', 5);
                    }
                }).catch(() => {
                    app.createTaskLoading = false;
                });
            },
            taskDone(taskCode, stageIndex, taskIndex, done) {
                let task = null;
                let unDoneTasks = this.taskStages[stageIndex].unDoneTasks;
                let doneTasks = this.taskStages[stageIndex].doneTasks;
                if (done) {
                    task = unDoneTasks[taskIndex];
                } else {
                    task = doneTasks[taskIndex];
                }
                // let task = this.taskStages[stageIndex].tasks[taskIndex];
                if (task.hasUnDone) {
                    return false;
                }
                task.done = done;
                if (done) {
                    unDoneTasks.splice(taskIndex, 1);
                    doneTasks.push(task);
                    doneTasks = doneTasks.sort(function (a, b) {
                        if (a.sort === b.sort) {
                            return a.id_num - b.id_num;
                        } else {
                            return a.sort - b.sort;
                        }
                    });
                } else {
                    doneTasks.splice(taskIndex, 1);
                    unDoneTasks.push(task);
                    unDoneTasks = unDoneTasks.sort(function (a, b) {
                        if (a.sort === b.sort) {
                            return a.id_num - b.id_num;
                        } else {
                            return a.sort - b.sort;
                        }
                    });
                }
                taskDone(taskCode, done).then((res) => {
                    const result = checkResponse(res);
                    if (!result) {
                        return false;
                    }
                    //Workflow may be triggered, so refresh all
                    this.getTaskStages(false);
                });
            },
            showInputStrageName() {
                this.showCreateStage = !this.showCreateStage;
                this.$nextTick(() => {
                    this.$refs.inputStageName.focus();
                });
            },
            doStage(action) {
                let app = this;
                let actionKeys = action.key.split('_');
                const stageCode = actionKeys[actionKeys.length - 2];
                const stageIndex = actionKeys[actionKeys.length - 1];
                const actionKey = actionKeys[0];
                switch (actionKey) {
                    case 'editStage':
                        this.stageModal.stageCode = stageCode;
                        this.stageModal.stageIndex = stageIndex;
                        this.$nextTick(() => {
                            this.stageModal.form.setFieldsValue({
                                name: this.taskStages[stageIndex].name,
                            });
                            this.$refs.inputStageTitle.focus();
                        });
                        this.stageModal.modalStatus = true;
                        break;
                    case 'recycleBatch':
                        //Are you sure you want to move all tasks under the list to Trash?
                        this.$confirm({
                            title: 'move to trash',
                            content: `Are you sure you want to move all tasks under the list to Trash?`,
                            okText: 'move to trash',
                            okType: 'danger',
                            cancelText: `Think again`,
                            onOk() {
                                app.taskStages[stageIndex].tasks = [];
                                app.$set(app.taskStages[stageIndex], 'doneTasks', []);
                                recycleBatch({stageCode: stageCode}).then(res => {
                                    const result = checkResponse(res);
                                    if (!result) {
                                        return false;
                                    }
                                    app.$set(app.taskStages[stageIndex], 'doneTasks', []);
                                    app.$set(app.taskStages[stageIndex], 'unDoneTasks', []);
                                });
                                return Promise.resolve();
                            }
                        });
                        break;
                    case 'setEndTime':
                        this.set_type_endTime_modal = true;
                        break;
                    case 'setExecutor':
                        this.projectMemberModal.currentStageIndex = stageIndex;
                        this.projectMemberModal.modalStatus = true;
                        break;
                    case 'delStage':
                        if (this.taskStages[stageIndex].tasks.length > 0) {
                            this.$warning({
                                title: 'delete list',
                                content: `Please clear tasks from this list before deleting this list`,
                                okText: 'Sure',
                            });
                            return false;
                        }
                        this.$confirm({
                            title: 'delete list',
                            content: `Are you sure you want to permanently delete this listing?`,
                            okText: 'delete',
                            okType: 'danger',
                            cancelText: `Think again`,
                            onOk() {
                                delStage(stageCode);
                                app.taskStages.splice(stageIndex, 1);
                                return Promise.resolve();
                            }
                        });
                        break;
                }
            },
            creteStage() {
                if (!this.stageName) {
                    this.$message.warning('Please enter a list name', 2);
                    return false;
                }
                createState({name: this.stageName, projectCode: this.code}).then(res => {
                    const result = checkResponse(res);
                    if (!result) {
                        return false;
                    }
                    const stage = res.data;
                    this.taskStages.push(stage);
                    this.stageName = '';
                    this.$nextTick(() => {
                        document.getElementById("board-scrum-stages").scrollLeft = 10000;
                    });
                });
            },
            editStage() {
                let stage = this.stageModal.form.getFieldsValue();
                if (!stage.name) {
                    this.$message.warning('Please enter a list name', 2);
                    return false;
                }
                editStage({name: stage.name, stageCode: this.stageModal.stageCode}).then((res) => {
                    const result = checkResponse(res);
                    if (!result) {
                        return false;
                    }
                    this.taskStages[this.stageModal.stageIndex].name = stage.name;
                    this.stageModal.modalStatus = false;
                });
            },
            setExecutor(member) {
                let stage = this.taskStages[this.projectMemberModal.currentStageIndex];
                let taskCodes = [];
                stage.tasks.forEach((v) => {
                    if (v.canRead) {
                        taskCodes.push(v.code);
                    }
                });
                if (taskCodes) {
                    batchAssignTask({taskCodes: JSON.stringify(taskCodes), executorCode: member.code}).then(res => {
                        this.projectMemberModal.modalStatus = false;
                        if (!checkResponse(res)) {
                            return false;
                        }
                        getTasks({stageCode: stage.code}).then((res) => {
                            let canNotReadCount = 0;
                            res.data.forEach((task) => {
                                if (!task.canRead) {
                                    canNotReadCount++;
                                }
                            });
                            stage.canNotReadCount = canNotReadCount;
                            stage.tasksLoading = false;
                            stage.tasks = res.data;
                        });
                    });
                } else {
                    this.projectMemberModal.modalStatus = false;
                }
            },
            showTaskPri(pri) {
                return {
                    'warning': pri == 1,
                    'error': pri == 2,
                }
            },
            showTimeLabel(time) {
                let str = 'label-primary';
                if (time == null) {
                    return str;
                }
                let cha = moment(moment(time).format("YYYY-MM-DD")).diff(moment().format("YYYY-MM-DD"), 'days');
                if (cha < 0) {
                    str = 'label-danger';
                } else if (cha == 0) {
                    str = 'label-warning';
                } else if (cha > 7) {
                    str = 'label-normal'
                }
                return str;
            },
            showTaskTime(time, timeEnd) {
                return formatTaskTime(time, timeEnd);
                // return moment(time).format('MM月DD日 HH:mm')
            },
            taskDetail(code, stageIndex) {
                this.$router.push(`${this.$route.path}/detail/${code}?from=${stageIndex}`);
            },
            stageMove(evt) {
                this.preCode = evt.draggedContext.element.code;
                this.nextCode = evt.relatedContext.element.code;

            },
            stageSort() {
                sort(this.preCode, this.nextCode, this.code);
            },
            taskSort(event) {
                console.log(event);
                const toStageCode = event.to.parentNode.parentNode.parentNode.getAttribute('id');
                let codes = '';
                for (let i = 0, len = event.to.children.length; i < len; i++) {
                    codes += ',' + event.to.children[i].getAttribute('id');
                }
                sortTask({stageCode: toStageCode, codes: codes.substr(1)}).then(res => {
                    this.getTaskStages(false);
                });
            },
            handleResize(vertical, stageIndex) {
                if (vertical.barSize) {
                    this.taskStages[stageIndex].fixedCreator = true;
                }
            },

            visibleDraw(type) {
                if (type == 'member') {
                    this.configDraw.visible = false;
                    this.taskSearch.visible = false;
                    this.inviteMemberDraw.visible = !this.inviteMemberDraw.visible;

                } else if (type == 'taskSearch') {
                    this.taskSearch.visible = !this.taskSearch.visible;
                    this.configDraw.visible = false;
                    this.inviteMemberDraw.visible = false;
                } else {
                    this.inviteMemberDraw.visible = false;
                    this.taskSearch.visible = false;
                    this.configDraw.visible = !this.configDraw.visible;
                }
            },
            removeMember(member, index) {
                let app = this;
                this.$confirm({
                    title: `Are you sure you want to remove「${member.name}」from project?`,
                    content: `After removal, the member will not be able to view any information about the project`,
                    okText: 'remove',
                    okType: 'danger',
                    cancelText: 'Think again',
                    onOk() {
                        removeMember(member.code, app.code).then((res) => {
                            if (!checkResponse(res)) {
                                return;
                            }
                            app.projectMembers.splice(index, 1);
                            notice({title: 'Removed successfully'}, 'notice', 'success');
                        });
                        return Promise.resolve();
                    }
                });
            },
            inviteMember(item) {
                inviteMember(item.memberCode, this.projectCode).then((res) => {
                    const success = checkResponse(res);
                    if (success) {
                        item.joined = true;
                    }
                })
            },
            searchInviteMember: _.debounce(
                function () {
                    if (!this.inviteMemberDraw.keyword) {
                        this.projectMembers = JSON.parse(JSON.stringify(this.projectMembersCopy));
                    }
                    /*if (this.inviteMemberDraw.keyword.length <= 1) {
                        return false;
                    }*/
                    this.searching = true;
                    this.projectMembers = this.projectMembers.filter(item => item.name.indexOf(this.inviteMemberDraw.keyword) != -1);
                }, 500
            ),
            updateProject(data) {
                this.project = data;
            },
            collectProject() {
                const type = this.project.collected ? 'cancel' : 'collect';
                collect(this.project.code, type).then((res) => {
                    if (!checkResponse(res)) {
                        return;
                    }
                    this.project.collected = !this.project.collected;
                })
            },
            handleChange(info) {
                if (info.file.status === 'uploading') {
                    notice(`Importing, please wait...`, 'message', 'loading', 0);
                    this.uploadLoading = true;
                    return
                }
                if (info.file.status === 'done') {
                    console.log(info);
                    this.uploadLoading = false;
                    if (checkResponse(info.file.response, true)) {
                        const count = info.file.response.data;
                        if (count) {
                            notice(`successfully imported${count}tasks`, 'message', 'success');
                        } else {
                            notice(`No tasks imported successfully`, 'message', 'warning');
                        }
                        this.getTaskStages(false);
                    }
                }
            },
            beforeUpload(file) {
                const isLt2M = file.size / 1024 / 1024 < 2;
                if (!isLt2M) {
                    this.$message.error('Files cannot exceed 2MB!')
                }
                return isLt2M
            },
            getStatusColor(status) {
                const statusInfo = this.taskStatusList.find(item => item.id == status);
                if (statusInfo) {
                    return statusInfo.color;
                }
                return '';
            }
        }
    }
</script>

<style lang="less">
    @import "../../../assets/css/components/task";

    .project-space-task {
        .tasks-loading {
            .ant-spin-blur {
                opacity: 0;
            }
        }
    }

    .info-drawer {
        top: 116px;
        width: 0 !important;

        .ant-drawer-mask {
            visibility: hidden;
        }

        .ant-drawer-content {
            /*background-color: #f7f7f7;*/
        }

        .ant-drawer-header {
            /*background-color: #f7f7f7;*/
            text-align: center;
        }

        .ant-drawer-body {
            padding: 12px 0;
        }

        .search-content {
            padding: 0 24px;
        }
    }

    .info-drawer {
        .member-list {
            padding-top: 12px;

            .ant-list-item-meta {
                align-items: center;
            }

            .member-list-item {
                padding: 12px 24px;

                &:hover {
                    background-color: #eee;
                    cursor: pointer;
                }
            }
        }
    }

    .info-drawer {
        .config-wrapper {
            position: relative;
            padding-bottom: 1px;

            .config-menus {
                padding: 0;
                list-style: none;

                .menu-item {
                    position: relative;
                    line-height: 30px;

                    &:hover {
                        background: #eeeeee;
                    }

                    &:first-child > a {
                        margin-top: -6px;
                    }

                    a {
                        display: block;
                        cursor: pointer;
                        padding: 5px 15px;
                        text-decoration: none;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        white-space: nowrap;
                        color: #383838;
                        font-weight: 600;

                        .anticon {
                            width: 24px;
                            text-align: center;
                            font-size: 15px;
                            margin-right: 5px;
                        }
                    }
                }
            }
        }
    }
</style>
