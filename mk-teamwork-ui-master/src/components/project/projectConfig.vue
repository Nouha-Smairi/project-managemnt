<template>
    <a-tabs tabPosition="left" defaultActiveKey="1" :animated="false" v-model="tabKey">
        <a-tab-pane key="1">
                    <span slot="tab">
                        <a-icon type="heat-map"/>
                        overview
                    </span>
            <div class="config-content">
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">project cover</p>
                        <div class="cover-item">
                            <template v-if="project.cover">
                                <img class="avatar" :src="project.cover"/>
                            </template>
                            <span class="no-avatar" v-show="!project.cover"></span>
                            <a-upload
                                    name="cover"
                                    class="cover-uploader"
                                    :showUploadList="false"
                                    :headers="headers"
                                    :action="uploadAction"
                                    :beforeUpload="beforeUpload"
                                    @change="handleChange"
                            >
                                <a-button size="large" class="upload">upload new cover</a-button>
                                <p class="upload-tips muted">The best image ratio is 300*150</p>
                            </a-upload>
                        </div>
                    </div>
                </div>
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">project name</p>
                        <a-input size="large" v-model="project.name"></a-input>
                    </div>
                    <div class="infos">
                        <p class="item-title">project progress (%)</p>
                        <a-input-number
                                size="large"
                                :min="0"
                                :max="100"
                                v-model="project.schedule"></a-input-number>
                    </div>
                </div>
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">Project Description</p>
                        <a-input type="textarea" :rows="3" size="large" placeholder="introduce this project"
                                 v-model="project.description"></a-input>
                    </div>
                </div>
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">Project openness</p>
                        <a-select
                                size="large"
                                v-model="project.privated"
                        >
                            <a-select-option :key="0">
                                Public project (accessible by link to everyone, editable only by project members)
                            </a-select-option>
                            <a-select-option :key="1">
                                Private projects (only project members can view and edit)
                            </a-select-option>
                        </a-select>
                    </div>
                </div>
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">project owner</p>
                        <div class="m-t">
                            <a-avatar :src="project.owner_avatar"></a-avatar>
                            <span class="m-l">{{project.owner_name}}</span>
                            <!--<a-button class="middle-btn pull-right" size="large">
                                移交
                            </a-button>-->
                        </div>
                    </div>
                </div>
                <a-divider/>
                <div class="content-item">
                    <div class="infos">
                        <a-button type="primary" class="middle-btn pull-right" size="large"
                                  @click="saveProject">
                                  save
                        </a-button>
                    </div>
                </div>
            </div>
        </a-tab-pane>
        <a-tab-pane key="2" forceRender>
             <span slot="tab">
                 <a-icon type="eye"/>
                 project preference
             </span>
            <div class="config-content task-config">
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">Kanban style</p>
                        <div class="item-tips muted">Switch the theme style of the task board, and there are two built-in styles.</div>
                        <a-radio-group @change="saveProject" v-model="project.task_board_theme">
                            <a-radio style="display: block;height: 30px;line-height: 30px" value="default">默认</a-radio>
                            <a-radio style="display: block;height: 30px;line-height: 30px" value="simple">简约</a-radio>
                        </a-radio-group>
                    </div>
                </div>
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">Automatically update project progress</p>
                        <div class="item-tips muted">Automatically calculate project progress based on the completion of current tasks.</div>
                    </div>
                    <a-switch v-model="project.auto_update_schedule" @change="saveProject"/>
                </div>
            </div>
        </a-tab-pane>
        <a-tab-pane key="3">
                    <span slot="tab">
                        <a-icon type="check-square"/>
                        task settings
                    </span>
            <div class="config-content task-config">
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">project ID</p>
                        <div class="item-tips muted">Set the project number, the number will be used as the ID prefix to distinguish the project. 2-6 characters are supported</div>
                        <a-input class="prefix-input" size="large" placeholder="For example, My Project can be set to MP"
                                 v-model="project.prefix"
                                 :disabled="!project.open_prefix"></a-input>
                        <a-button size="large" class="middle-btn" type="primary"
                                  :disabled="!project.open_prefix" @click="saveProject">save
                        </a-button>
                    </div>
                    <a-switch v-model="project.open_prefix" @change="saveProject"/>
                </div>
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">task start time</p>
                        <div class="item-tips muted">Set start times for tasks and plan work more scientifically.</div>
                    </div>
                    <a-switch v-model="project.open_begin_time" @change="saveProject"/>
                </div>
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">Private mode is enabled by default for new tasks</p>
                        <div class="item-tips muted">The privacy mode is turned on by default for new tasks in this project, and only participants can see them after they are successfully created</div>
                    </div>
                    <a-switch v-model="project.open_task_private" @change="saveProject"/>
                </div>
            </div>
        </a-tab-pane>
        <a-tab-pane key="4" forceRender>
             <span slot="tab">
                <a-icon type="deployment-unit"/>
                Task flow
             </span>
            <div class="config-content">
                <div class="content-item task-workflow">
                    <div class="infos" style="padding-right: 0">
                        <div v-show="!doTaskWorkflowView">
                            <p>
                                <a-button type="primary" icon="plus" @click="doTaskWorkflow(null)">create rules</a-button>
                            </p>
                            <a-list
                                    class="task-workflow-list"
                                    itemLayout="horizontal"
                                    :dataSource="taskWorkflowList"
                            >
                                <a-list-item slot="renderItem" slot-scope="item">
                                <span slot="actions">
                                     <a-tooltip placement="top"
                                                title="edit">
                                        <a class="muted m-r-sm" @click="doTaskWorkflow(item)"><a-icon
                                                                                               type="edit"></a-icon></a>
                                    </a-tooltip>
                                     <a-tooltip placement="top"
                                                title="delete">
                                         <a class="muted" @click="delTaskWorkflow(item.code)">
                                             <a-icon type="delete"></a-icon>
                                         </a>
                                    </a-tooltip>
                                </span>
                                    <a-list-item-meta
                                    >
                                        <span slot="title">{{item.name}}</span>
                                    </a-list-item-meta>
                                </a-list-item>
                            </a-list>
                        </div>
                        <div v-show="doTaskWorkflowView">
                            <div class="header">
                                <p>
                                    <a-button size="large" class="middle-btn m-r-sm" @click="saveTaskWorkflow(false)">
                                        Cancel
                                    </a-button>
                                    <a-button size="large" class="middle-btn" type="primary"
                                              @click="saveTaskWorkflow(true)" :disabled="!canSaveTaskWorkflow">save
                                    </a-button>
                                </p>
                            </div>
                            <div class="workflow-content">
                                <template v-if="!loadingWorkflowRule">
                                    <div class="workflow-rule-item">
                                        <p>
                                            rule name
                                        </p>
                                        <a-input size="large" v-model="currentTaskWorkflowRule.taskWorkflowName"></a-input>
                                    </div>
                                    <div class="workflow-rule-item">
                                        <p>Select task list</p>
                                        <a-select size="large" v-model="currentTaskWorkflowRule.firstObj"
                                                  @change="(value)=>workflowRuleChange(value,'firstObj')">
                                            <a-select-option v-for="(taskStage, index) in taskStages"
                                                             :value="taskStage.code" :key="taskStage.code">
                                                {{taskStage.name}}
                                            </a-select-option>
                                        </a-select>
                                    </div>

                                    <template v-if="currentTaskWorkflowRule.firstObj">
                                        <div class="workflow-rule-item">
                                            <p>Selection criteria</p>
                                            <a-select size="large" v-model="currentTaskWorkflowRule.firstAction.action"
                                                      @change="(value)=>workflowRuleChange(value,'firstAction.action')">
                                                <a-select-option v-for="(rule, index) in taskWorkflowRuleActions"
                                                                 :value="rule.id" :key="rule.id">{{rule.name}}
                                                </a-select-option>
                                            </a-select>
                                        </div>

                                        <template v-if="currentTaskWorkflowRule.firstAction.action != -1">
                                            <template v-if="currentTaskWorkflowRule.firstAction.action == 3">
                                                <div class="workflow-rule-item">
                                                    <p>choose executor</p>
                                                    <a-select size="large" v-model="currentTaskWorkflowRule.firstAction.value"
                                                              @change="(value)=>workflowRuleChange(value,'firstAction.value')">
                                                        <a-select-option v-for="(member, index) in projectMembers"
                                                                         :value="member.code" :key="member.code">
                                                            {{member.name}}
                                                        </a-select-option>
                                                    </a-select>
                                                </div>
                                            </template>

                                            <div class="workflow-rule-item">
                                                <p>select result</p>
                                                <a-select size="large" v-model="currentTaskWorkflowRule.firstResult.action"
                                                          @change="(value)=>workflowRuleChange(value,'firstResult.action')">
                                                    <a-select-option v-for="(type, index) in taskWorkflowRuleTypes"
                                                                     :value="type.id" :key="type.id">{{type.name}}
                                                    </a-select-option>
                                                </a-select>
                                            </div>

                                            <template v-if="currentTaskWorkflowRule.firstResult.action === 0">
                                                <div class="workflow-rule-item">
                                                    <p>Circulation task list</p>
                                                    <a-select size="large" v-model="currentTaskWorkflowRule.firstResult.value"
                                                              @change="(value)=>workflowRuleChange(value,'firstResult.value')">
                                                        <a-select-option v-for="(taskStage, index) in taskStages"
                                                                         :value="taskStage.code" :key="taskStage.code">
                                                            {{taskStage.name}}
                                                        </a-select-option>
                                                    </a-select>
                                                </div>
                                                <template v-if="currentTaskWorkflowRule.firstResult.value">
                                                    <div class="workflow-rule-item">
                                                        <p>choose executor</p>
                                                        <a-select size="large" v-model="currentTaskWorkflowRule.lastResult.value"
                                                                  @change="(value)=>workflowRuleChange(value,'lastResult.value')">
                                                            <a-select-option v-for="(member, index) in projectMembers"
                                                                             :value="member.code" :key="member.code">
                                                                {{member.name}}
                                                            </a-select-option>
                                                        </a-select>
                                                    </div>
                                                </template>
                                                <template>
                                                    <div class="workflow-rule-item">
                                                        <p>Modify task status</p>
                                                        <a-select size="large" v-model="currentTaskWorkflowRule.state.value"
                                                                  @change="(value)=>workflowRuleChange(value,'state.value')">
                                                            <a-select-option v-for="(state, index) in taskStates"
                                                                             :value="state.id" :key="state.id">
                                                                {{state.name}}
                                                            </a-select-option>
                                                        </a-select>
                                                    </div>
                                                </template>
                                            </template>
                                            <template v-if="currentTaskWorkflowRule.firstResult.action === 3">
                                                <div class="workflow-rule-item">
                                                    <p>choose executor</p>
                                                    <a-select size="large" v-model="currentTaskWorkflowRule.firstResult.value"
                                                              @change="(value)=>workflowRuleChange(value,'firstAction.action')">
                                                        <a-select-option v-for="(member, index) in projectMembers"
                                                                         :value="member.code" :key="member.code">
                                                            {{member.name}}
                                                        </a-select-option>
                                                    </a-select>
                                                </div>

                                                <template v-if="currentTaskWorkflowRule.firstResult.value">
                                                    <div class="workflow-rule-item">
                                                        <p>Circulation task list</p>
                                                        <a-select size="large" v-model="currentTaskWorkflowRule.lastResult.value"
                                                                  @change="(value)=>workflowRuleChange(value,'lastResult.value')">
                                                            <a-select-option v-for="(taskStage, index) in taskStages"
                                                                             :value="taskStage.code" :key="taskStage.code">
                                                                {{taskStage.name}}
                                                            </a-select-option>
                                                        </a-select>
                                                    </div>
                                                </template>
                                                <template>
                                                    <div class="workflow-rule-item">
                                                        <p>Modify task status</p>
                                                        <a-select size="large" v-model="currentTaskWorkflowRule.state.value"
                                                                  @change="(value)=>workflowRuleChange(value,'state.value')">
                                                            <a-select-option v-for="(state, index) in taskStates"
                                                                             :value="state.id" :key="state.id">
                                                                {{state.name}}
                                                            </a-select-option>
                                                        </a-select>
                                                    </div>
                                                </template>
                                            </template>
                                        </template>

                                    </template>
                                </template>
                                <div class="text-center">
                                    <a-spin :spinning="loadingWorkflowRule"></a-spin>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </a-tab-pane>
        <a-tab-pane key="5">
                    <span slot="tab">
                        <a-icon type="ellipsis"/>
                        更多
                    </span>
            <div class="config-content more-config">
                <div class="content-item">
                    <div class="infos">
                        <p class="item-title">Project operation</p>
                        <div class="item-tips muted">You can do the following</div>
                        <a-button size="large" class="middle-btn" @click="archiveProject">
                            <span v-if="project.archive">unarchive</span>
                            <span v-if="!project.archive">Archive items</span>
                        </a-button>
                        <a-button size="large" class="middle-btn" @click="quitProject">quit</a-button>
                        <a-button size="large" class="middle-btn" type="danger" @click="delProject">
                            <span v-if="project.deleted">recovery item</span>
                            <span v-if="!project.deleted">move to trash</span>
                        </a-button>
                    </div>
                </div>
            </div>
        </a-tab-pane>
    </a-tabs>
</template>

<script>
    import {read as getProject, doData, archive, recycle, recoveryArchive, recovery, quit} from "../../api/project";
    import {
        _getTaskWorkflowRules,
        list as getTaskWorkflowList,
        save as saveTaskWorkflow,
        edit as EditTaskWorkflow,
        del as delTaskWorkflow
    } from "../../api/taskWorkflow";
    import {_getAll as getTaskStages} from "../../api/taskStages";
    import {list as getProjectMembers} from "../../api/projectMember";

    import {notice} from "../../assets/js/notice";
    import {checkResponse, getApiUrl, getAuthorization, getBase64} from "../../assets/js/utils";

    export default {
        name: "projectConfig",
        props: {
            code: {
                type: [String],
                default() {
                    return ''
                }
            },
        },
        data() {
            return {
                /*project settings*/
                loading: false,
                tabKey: '1',
                project: {},
                taskWorkflowList: [],
                doTaskWorkflowView: false,
                loadingWorkflowRule: false,
                taskWorkflow: null,
                taskWorkflowRuleList: [],
                currentTaskWorkflowRule: {
                    taskWorkflowName: '',
                    firstObj: '',//which list task
                    firstAction: {//do what
                        action: -1,
                        value: ''
                    },
                    firstResult: {//At once
                        action: -1,
                        value: ''
                    },
                    lastResult: {//at last
                        action: -1,
                        value: ''
                    },
                    state: {//task status
                        action: -1,
                        value: -1
                    },
                },
                taskWorkflowRuleActions: [
                    {id: -1, name: "please choose"},
                    {id: 0, name: "Add tasks"},
                    {id: 1, name: "be completed"},
                    //{id: 2, name: "be redone"},
                    {id: 3, name: "set executor"},
                    // {id: 4, name: "deadline"},
                    // {id: 5, name: "priority"},
                ],
                taskWorkflowRuleTypes: [
                    {id: -1, name: "please choose"},
                    {id: 0, name: "Automatic flow to"},
                    {id: 3, name: "By default assigned to"},
                ],
                taskStates: [
                    {id: -1, name: "no modification"},
                    {id: 1, name: "completed"},
                    {id: 2, name: "undone"},
                ],
                projectMembers: [],
                taskStages: [],
                uploadLoading: false,
                uploadAction: getApiUrl('project/project/uploadCover'),
            }
        },
        computed: {
            headers() {
                return getAuthorization();
            },
            canSaveTaskWorkflow() {
                return this.currentTaskWorkflowRule.taskWorkflowName.trim() && this.currentTaskWorkflowRule.firstObj && this.currentTaskWorkflowRule.firstAction.action != -1 && this.currentTaskWorkflowRule.firstResult.action != -1 && this.currentTaskWorkflowRule.firstResult.value;
            }
        },
        watch: {
            code() {
                this.getProject();
            }
        },
        created() {
            this.getProject();
            this.getTaskWorkflowList();
        },
        methods: {
            getProject() {
                this.loading = true;
                getProject(this.code).then((res) => {
                    this.loading = false;
                    this.project = res.data;
                    this.project.open_prefix = !!res.data.open_prefix;
                    this.project.open_begin_time = !!res.data.open_begin_time;
                    this.project.open_task_private = !!res.data.open_task_private;
                    this.project.auto_update_schedule = !!res.data.auto_update_schedule;
                });
            },
            getTaskWorkflowList() {
                getTaskWorkflowList({projectCode: this.code}).then(res => {
                    this.taskWorkflowList = res.data;
                });
            },
            saveProject() {
                const project = this.project;
                doData({
                    projectCode: project.code,
                    name: project.name,
                    description: project.description,
                    cover: project.cover,
                    privated: project.privated,
                    prefix: project.prefix,
                    task_board_theme: project.task_board_theme,
                    open_prefix: Number(project.open_prefix),
                    open_begin_time: Number(project.open_begin_time),
                    open_task_private: Number(project.open_task_private),
                    schedule: Number(project.schedule),
                    auto_update_schedule: Number(project.auto_update_schedule),
                }).then((res) => {
                    if (!checkResponse(res)) {
                        return;
                    }
                    this.$emit('update', this.project);
                    notice({
                        title: 'Saved successfully',
                    }, 'notice', 'success');
                })
            },
            archiveProject() {
                let app = this;
                if (!app.project.archive) {
                    this.$confirm({
                        title: 'Archive items',
                        content: `once the project「${this.project.name}」is archived，This item and the information contained in it will be moved to the archived item, and the content will still be included in the statistics and search, and the archived item can be restored and continued to be used at any time.`,
                        okText: 'archive',
                        okType: 'danger',
                        cancelText: `Think again`,
                        onOk() {
                            archive(app.code).then((res) => {
                                if (!checkResponse(res)) {
                                    return;
                                }
                                app.$router.replace('/project/archive');
                            });
                            return Promise.resolve();
                        }
                    });
                } else {
                    this.$confirm({
                        title: 'Unarchive project?',
                        content: `unarchive「${this.project.name}」After that, it can be used normally`,
                        okText: 'unarchive',
                        okType: 'primary',
                        cancelText: 'Think again',
                        onOk() {
                            recoveryArchive(app.code).then((res) => {
                                if (!checkResponse(res)) {
                                    return;
                                }
                                app.$router.replace('/project/list/my');
                            });
                            return Promise.resolve();
                        }
                    });
                }
            },
            delProject() {
                let app = this;
                if (!app.project.deleted) {
                    this.$confirm({
                        title: 'Are you sure you want to put in the recycle bin?',
                        content: `Once the project「${this.project.name}」Put into the recycle bin, all information related to the project will be put into the recycle bin`,
                        okText: 'put in trash',
                        okType: 'danger',
                        cancelText: 'Think again',
                        onOk() {
                            recycle(app.code).then((res) => {
                                if (!checkResponse(res)) {
                                    return;
                                }
                                app.$router.replace('/project/recycle');
                            });
                            return Promise.resolve();
                        }
                    });
                } else {
                    this.$confirm({
                        title: 'Are you sure you want to restore the project?',
                        content: `recover「${this.project.name}」After that, it can be used normally`,
                        okText: 'recovery item',
                        okType: 'primary',
                        cancelText: 'Think again',
                        onOk() {
                            recovery(app.code).then((res) => {
                                if (!checkResponse(res)) {
                                    return;
                                }
                                app.$router.replace('/project/list/my');
                            });
                            return Promise.resolve();
                        }
                    });
                }
            },
            quitProject() {
                let app = this;
                this.$confirm({
                    title: 'quit project',
                    content: `Once you exit the project, you will not be able to view any information about the project. After leaving the project, if you want to rejoin, please contact the project administrator.`,
                    okText: 'OK to exit',
                    okType: 'primary',
                    cancelText: 'Think again',
                    onOk() {
                        quit(app.code).then((res) => {
                            if (!checkResponse(res)) {
                                return;
                            }
                            app.$router.replace('/project/list/my');

                        });
                        return Promise.resolve();
                    }
                });
            },
            doTaskWorkflow(taskWorkflow = null) {
                this.getTaskStages();
                this.getProjectMembers();
                this.taskWorkflow = taskWorkflow;
                if (taskWorkflow) {
                    this.loadingWorkflowRule = true;
                    this.currentTaskWorkflowRule.taskWorkflowName = taskWorkflow.name;
                    this.getTaskWorkflowRules(taskWorkflow.code);
                }else{
                    this.currentTaskWorkflowRule = {
                        taskWorkflowName: '',
                        firstObj: '',//which list task
                        firstAction: {//do what
                            action: -1,
                            value: ''
                        },
                        firstResult: {//At once
                            action: -1,
                            value: ''
                        },
                        lastResult: {//at last
                            action: -1,
                            value: ''
                        },
                        state: {//task status
                            action: -1,
                            value: -1
                        },
                        taskStates: [
                            {id: -1, name: "no modification"},
                            {id: 1, name: "completed"},
                            {id: 2, name: "undone"},
                        ],
                    };
                }
                this.doTaskWorkflowView = true;
            },
            saveTaskWorkflow(save = false) {
                if (this.currentTaskWorkflowRule.firstResult.action) {
                    this.currentTaskWorkflowRule.lastResult.action = 0;
                } else {
                    this.currentTaskWorkflowRule.lastResult.action = 3;
                }
                let submitData = {
                    taskWorkflowName: this.currentTaskWorkflowRule.taskWorkflowName,
                    taskWorkflowRules: JSON.stringify(this.currentTaskWorkflowRule)
                };
                if (save) {
                    // save
                    if (this.taskWorkflow) {
                        submitData.taskWorkflowCode = this.taskWorkflow.code;
                        EditTaskWorkflow(submitData).then(res => {
                            this.getTaskWorkflowList();

                        });
                    } else {
                        submitData.projectCode = this.code;
                        saveTaskWorkflow(submitData).then(res => {
                            this.getTaskWorkflowList();
                        });
                    }

                }
                this.doTaskWorkflowView = false;
            },
            getTaskStages() {
                getTaskStages({projectCode: this.code}).then(res => {
                    let list = [{
                        code: '',
                        name: 'do not choose'
                    }];
                    res.data.forEach(v => {
                        list.push(v);
                    });
                    this.taskStages = list;
                })
            },
            getTaskWorkflowRules(taskWorkflowCode) {
                _getTaskWorkflowRules({taskWorkflowCode: taskWorkflowCode}).then(res => {
                    this.loadingWorkflowRule = false;
                    const rules = res.data;
                    this.taskWorkflowRuleList = rules;
                    if (rules) {
                        this.currentTaskWorkflowRule.firstObj = rules[0].object_code;
                        this.currentTaskWorkflowRule.firstAction.action = rules[1].action;
                        this.currentTaskWorkflowRule.firstAction.value = rules[1].object_code;

                        this.currentTaskWorkflowRule.firstResult.action = rules[2].action;
                        this.currentTaskWorkflowRule.firstResult.value = rules[2].object_code;

                        if (rules.length >= 4) {
                            if (!rules[3].object_code) {
                                this.currentTaskWorkflowRule.state.action = rules[3].action;
                                this.currentTaskWorkflowRule.state.value = rules[3].action;
                            }else{
                                this.currentTaskWorkflowRule.lastResult.action = rules[3].action;
                                this.currentTaskWorkflowRule.lastResult.value = rules[3].object_code;
                            }
                            if (rules.length >= 5) {
                                if (!rules[4].object_code) {
                                    this.currentTaskWorkflowRule.state.action = rules[4].action;
                                    this.currentTaskWorkflowRule.state.value = rules[4].action;
                                }else{
                                    this.currentTaskWorkflowRule.lastResult.action = rules[4].action;
                                    this.currentTaskWorkflowRule.lastResult.value = rules[4].object_code;
                                }
                            }
                        }
                    }
                })
            },
            delTaskWorkflow(code) {
                let app = this;
                this.$confirm({
                    title: 'Delete rules?',
                    content: 'Are you sure you want to delete this rule?',
                    okText: 'delete',
                    okType: 'danger',
                    cancelText: 'Think again',
                    onOk() {
                        delTaskWorkflow({taskWorkflowCode: code}).then(res => {
                            if (checkResponse(res)) {
                                app.getTaskWorkflowList();
                            }
                        });
                        return Promise.resolve();
                    }
                });
            },
            getProjectMembers() {
                getProjectMembers({projectCode: this.code, pageSize: 100}).then((res) => {
                    let list = [{
                        code: '',
                        name: 'do not choose'
                    }];
                    res.data.list.forEach(v => {
                        list.push(v);
                    });
                    this.projectMembers = list;
                });
            },
            workflowRuleChange(value, name) {
                console.log(value);
                console.log(name);
                if (name == 'firstAction.action') {
                    this.currentTaskWorkflowRule.firstAction.value = '';
                }
                if (name == 'firstResult.value') {
                    this.currentTaskWorkflowRule.lastResult.value = '';
                }
                if (name == 'firstResult.action') {
                    this.currentTaskWorkflowRule.firstResult.value = '';
                    this.currentTaskWorkflowRule.lastResult.value = '';
                }
            },
            handleChange(info) {
                if (info.file.status === 'uploading') {
                    this.uploadLoading = true;
                    return
                }
                if (info.file.status === 'done') {
                    getBase64(info.file.originFileObj, () => {
                        this.project.cover = info.file.response.data.url;
                        this.uploadLoading = false;
                        notice({
                            title: 'Cover uploaded successfully',
                        }, 'notice', 'success');
                    })
                }
            },
            beforeUpload(file) {
                const isLt2M = file.size / 1024 / 1024 < 2;
                if (!isLt2M) {
                    this.$message.error('Images cannot exceed 2MB!')
                }
                return isLt2M
            },
        }
    }
</script>

<style lang="less">
    .project-config-modal {
        .ant-modal-body {
            padding: 0;
        }

        .ant-modal-body, .ant-tabs, .ant-tabs-bar {
            min-height: 730px;
            min-width: 180px;

            .ant-tabs-tab {
                padding: 12px 24px;
            }
        }

        .ant-tabs-nav-wrap {
            padding-top: 10px;
        }

        .ant-tabs-left-content {
            padding-top: 18px;
            padding-right: 24px;
        }

        .ant-tabs .ant-tabs-left-bar .ant-tabs-tab {
            text-align: left;
        }

        .config-content {
            .content-item {
                display: flex;
                justify-content: space-between;
                flex: 1 1;
                margin-bottom: 24px;

                .infos {
                    width: 100%;
                    padding-right: 12px;

                    p {
                        margin-bottom: 6px;
                    }

                    .item-title {
                        font-size: 16px;
                    }

                    .item-tips {
                        margin-bottom: 12px;
                    }

                    .ant-select {
                        width: 100%;
                    }

                    .ant-input-number-lg {
                        width: 100%;
                    }

                    .cover-item {
                        display: flex;

                        img {
                            width: 300px;
                            height: 150px;
                        }

                        .cover-uploader {
                            display: block;
                            margin-left: 24px;

                            .upload-tips {
                                margin-top: 12px;
                            }
                        }
                    }
                }
            }

            &.task-config {
                .content-item {
                    padding-bottom: 24px;
                    padding-right: 16px;
                    border-bottom: 1px solid #e5e5e5;
                }

                .prefix-input {
                    width: 50%;
                    margin-right: 24px;
                }
            }

            .task-workflow {
                .workflow-content {
                    margin-top: 12px;

                    .workflow-rule-item {
                        margin-bottom: 16px;

                        p {
                            color: rgba(0, 0, 0, 0.45);
                        }
                    }
                }
            }

            &.more-config {
                .ant-btn {
                    margin-right: 12px;
                }
            }
        }
    }
</style>
