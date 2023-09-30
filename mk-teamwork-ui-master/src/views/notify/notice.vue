<template>
    <div class="house-index">
        <wrapper-content>
            <div class="page-search">
                <a-form
                        layout="inline"
                        :autoFormCreate="(form)=>{this.searchForm = form}"
                        @submit.prevent="handleSearchSubmit"
                >
                    <a-form-item
                            label='keywords'
                            fieldDecoratorId="title"
                    >
                        <a-input placeholder='Please enter key words'/>
                    </a-form-item>
                    <a-form-item
                            label='date'
                            fieldDecoratorId="date"
                    >
                        <a-range-picker :placeholder="['start date','end date']"></a-range-picker>
                    </a-form-item>
                    <a-form-item
                    >
                        <a-button icon="search" type="primary" htmlType='submit'
                                :loading="searchLoading"
                        >search
                        </a-button>
                    </a-form-item>
                </a-form>
            </div>
            <div class="action">
                <a-button icon="check" class="m-r-sm" :disabled="!selectedRowKeys.length" @click="listAction({key:'setReadied'})">
                    <span>batch mark read</span>
                </a-button>
                <a-button icon="delete" type="danger" :disabled="!selectedRowKeys.length" @click="listAction({key:'delete'})">
                    <span>batch deletion</span>
                </a-button>
                <span style="padding-left: 12px;" v-show="selectedRowKeys.length">chosen <span class="text-warning">{{selectedRowKeys.length}}</span> item</span>
            </div>
            <a-table :columns="columns" :dataSource="dataSource" :loading="loading" :showTotal="pagination.showTotal" :pagination="pagination"
                     @change="pageChange"
                     :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
                     rowKey="id">
                <p slot="expandedRowRender" slot-scope="record" style="margin: 0" v-html="record.content"></p>
                <template slot="action" slot-scope="text,record,index">
                    <a @click="rowClick(record,'del')">delete</a>
                </template>
            </a-table>
        </wrapper-content>
    </div>
</template>
<script>
    import {list, del} from '@/api/notify';
    import {checkResponse} from '@/assets/js/utils';
    import {batchDel, setReadied} from "../../api/notify";
    import pagination from "@/mixins/pagination";

    const columns = [{
        title: 'message title',
        dataIndex: 'title',
        width: '20%',
    },{
        title: 'time',
        dataIndex: 'create_time',
    },{
        title: 'operate',
        scopedSlots: {
            customRender: 'action'
        }
    }];

    export default {
        mixins: [pagination],
        data() {
            return {
                columns,
                dataSource: [],
                selectedRowKeys: [],
                loading: true,
                searchForm: {},
                searchLoading: false,
            }
        },
        created() {
            this.init();
        },
        methods: {
            init() {
                let app = this;
                app.requestData.type = 'message';
                app.loading = true;
                app.selectedRowKeys = [];
                list(app.requestData).then(res => {
                    app.dataSource = res.data.list;
                    app.pagination.total = res.data.total;
                    app.loading = false;
                });
            },
            onSelectChange(selectedRowKeys) {
                this.selectedRowKeys = selectedRowKeys
            },
            listAction(type) {
                let app = this;
                switch (type.key) {
                    case 'setReadied':
                        setReadied(JSON.stringify(app.selectedRowKeys));
                        app.dataSource.forEach(function (v, k) {
                            const index = app.selectedRowKeys.find(item => item == v.id);
                            if (index) {
                                app.dataSource[k].is_read = 1;
                            }
                        });
                        app.selectedRowKeys = [];
                        app.$notice('Successful operation', 'message', 'success');
                        break;
                    case 'delete':
                        this.$confirm({
                            title: 'Are you sure you want to delete these messages?',
                            content: 'Unrecoverable after deletion',
                            okText: 'delete',
                            okType: 'danger',
                            cancelText: 'Cancel',
                            onOk() {
                                batchDel(JSON.stringify(app.selectedRowKeys)).then(res => {
                                    if (checkResponse(res)) {
                                        app.init();
                                        app.notice('Successful operation', 'message', 'success');
                                        app.selectedRowKeys = [];
                                    }
                                });
                                return Promise.resolve();
                            }
                        });
                        break;
                }
            },
            rowClick(record, action) {
                let app = this;
                if (action == 'del') {
                    this.$confirm({
                        title: 'Are you sure to delete this message?',
                        content: 'Unrecoverable after deletion',
                        okText: 'delete',
                        okType: 'danger',
                        cancelText: 'Cancel',
                        onOk() {
                            del(record.id).then(res => {
                                app.init();
                            });
                            return Promise.resolve();
                        }
                    });
                }

            },
            handleSearchSubmit() {
                let app = this;
                app.searchForm.validateFields(
                    (err, values) => {
                        if (!err) {
                            app.search();
                        }
                    })
            },
            search(){
                let obj = this.searchForm.getFieldsValue();
                if (obj.date && obj.date.length > 0) {
                    const beginDate = obj.date[0].format('YYYY-MM-DD');
                    const endDate = obj.date[1].format('YYYY-MM-DD');
                    obj.date = beginDate + '~' + endDate;
                }
                Object.assign(this.requestData, obj);
                this.init();
            }
        }
    }
</script>
