<template>
    <div class="admin-menu">
        <wrapper-content>
            <div class="action">
                <Button type="primary" icon="delete" @click="clearNode">Clean up invalid records</Button>
            </div>
            <Table v-if="data.length" :columns="columns" :dataSource="data" rowKey="node" defaultExpandAllRows :pagination="false">
                <template slot="titles" slot-scope="text, record">
                    <EditableCell :text="record.title ? record.title : '-'" @change="onCellChange($event,record.node)"/>
                </template>
                <template slot="action" slot-scope="text,record,index">
                    <div v-if="record.node.indexOf('/') !== -1">
                        <span v-if="!record.children && record.pnode" style="margin-left: 24px;"></span>
                        <Checkbox :checked="isChecked(record,'is_login')" @change="onChange($event,record,'is_login')">
                            <span v-if="record.children">all</span>join login control
                        </Checkbox>
                        <span v-if="!record.children && record.pnode" style="margin-left: 24px;"></span>
                        <Checkbox :checked="isChecked(record,'is_auth')" @change="onChange($event,record,'is_auth')">
                            <span v-if="record.children">all</span>Add permission control
                        </Checkbox>
                        <span v-if="!record.children && record.pnode" style="margin-left: 24px;"></span>
                       <!-- <Checkbox :checked="isChecked(record,'is_menu')" @change="onChange($event,record,'is_menu')">
                            <span v-if="record.children">all</span>Add menu node selector
                        </Checkbox>-->
                    </div>
                </template>
            </Table>
        </wrapper-content>
    </div>
</template>
<script>
    import {Table, Button, Divider, Modal, Form, Input, Checkbox} from 'ant-design-vue';
    import EditableCell from '@/components/table/EditableCell';
    import {getNodeList, save, clear} from '@/api/node';
    import {createRoute, checkResponse} from '@/assets/js/utils';
    import {getStore} from '@/assets/js/storage';

    const FormItem = Form.Item;

    const columns = [{
        title: 'node',
        dataIndex: 'node',
        width: '30%',
    }, {
        title: 'name',
        width: '12%',
        scopedSlots: {
            customRender: 'titles'
        },
    }, {
        title: 'operate',
        scopedSlots: {
            customRender: 'action'
        },
    },];
    export default {
        components: {
            Table,
            Button,
            Divider,
            Modal,
            Form,
            FormItem,
            Input,
            EditableCell,
            Checkbox
        },
        data() {
            return {
                columns,
                data: [],
                loading: true,
            }
        },
        created() {
            this.init()
        },
        methods: {
            init() {
                let app = this;
                app.$store.dispatch('pageLoading', true);
                getNodeList().then(res => {
                    app.$store.dispatch('pageLoading', false);
                    app.data = res.data.nodes;
                })
            },
            clearNode() {
                clear();
            },
            onCellChange(value, node) {
                save(JSON.stringify([{name: [{name: 'title'}], value: [{value: value}], node: node}]));
            },
            onChange(event, record, key) {
                const checked = event.target.checked;
                record[key] = checked;
                let data = [];
                let objFir = {name: [{name: key}], value: [{value: checked}], node: record.node};
                if (record.children) {
                    record.children.forEach(function (v) {
                        v[key] = checked;
                        let obj = {name: [{name: key}], value: [{value: checked}], node: v.node};
                        if (checked && key === 'is_auth') {
                            //If the permission control is checked, login verification is generally required
                            v['is_login'] = checked;
                            obj.name.push({name: 'is_login'});
                            obj.value.push({value: checked});
                        }
                        data.push(obj);
                    });
                }
                if (checked && key === 'is_auth') {
                    //If the permission control is checked, login verification is generally required
                    record['is_login'] = checked;
                    objFir.name.push({name: 'is_login'});
                    objFir.value.push({value: checked});
                }
                data.push(objFir);
                if (data) {
                    save(JSON.stringify(data));
                }
            },
            rowClick(record, action) {
                let app = this;
            },
            isChecked(record, key) {
                //Determine whether all child elements are selected
                if (!record.children) {
                    return !!record[key];
                }
                let checkedNum = 0;
                record.children.forEach(function (v) {
                    if (v[key]) {
                        checkedNum++;
                    }
                });
                return checkedNum === record.children.length;

            }
        }
    }
</script>
