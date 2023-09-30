<template>
    <div :style="styleObject">
        <div :id="id" ref="editor" :style="styleObject"></div>
    </div>
</template>

<script>
    import Wangeditor from 'wangeditor'
    import _ from "lodash"

    export default {
        name: 'editor',
        props: ['id', 'value', 'width', 'height', 'uploadImgServer', 'disabled', 'menus', 'mapKey', 'menuFixed', 'pasteFilter', 'codeDefaultLang', 'hideLinkImg', 'uploadImgParams', 'uploadImgHeaders', 'isRealtime', 'disabledMenus', 'uploadFileName', 'uploadImgShowBase64'],
        data() {
            return {
                styleObject: {
                    width: 'auto',
                    height: 'auto'
                },
                editor: null
            }
        },
        methods: {
            // set size
            initSize() {
                if (this.width) {
                    this.styleObject.width = this.width + 'px'
                }
                if (this.height) {
                    this.styleObject.height = this.height + 'px'
                }
            },
            // create editor
            createEditor() {
                this.editor = new Wangeditor(document.getElementById(this.id));
                this.initConfig();
                // console.log(this.editor);
                this.editor.create();
            },
            initConfig() {
                // this.editor.customConfig.uploadFileName = this.uploadFileName ? this.uploadFileName : 'image[]';
                // this.editor.customConfig.uploadImgServer = this.uploadImgServer;
                // this.editor.customConfig.uploadImgShowBase64 = this.uploadImgShowBase64 ? this.uploadImgShowBase64 : false;
                // this.editor.customConfig.uploadImgFns.onload = (resultText, xhr) => {
                //     let originalName = this.editor.uploadImgOriginalName || '';
                //     this.$emit('load', originalName, resultText)
                // };
                // this.editor.customConfig.uploadImgFns.ontimeout = (xhr) => {
                //     this.$emit('timeout')
                // };
                // this.editor.customConfig.uploadImgFns.onerror = (xhr) => {
                //     this.$emit('error')
                // };
                // this.editor.customConfig.uploadImgFileName = 'file';
                this.editor.customConfig.pasteFilter = false;
                if (this.mapKey != undefined) {
                    // Configure map key, empty by default
                    this.editor.customConfig.mapAk = this.mapKey
                }
                if (this.menus != undefined && this.menus instanceof Array && this.menus.length) {
                    // Configuration menu, default all, filter source
                    this.editor.customConfig.menus = this.filterMenu(this.menus)
                } else if (this.disabledMenus) {
                    // disable menu
                    this.editor.customConfig.menus = this.filterDisabledMenu(this.editor.customConfig.menus, this.disabledMenus)
                } else {
                    // this.editor.customConfig.menus = this.filterMenu(wangEditor.config.menus)
                }
                if (this.menuFixed != undefined) {
                    // Configure the ceiling of the menu bar, the default is true
                    this.editor.customConfig.menuFixed = this.menuFixed
                }
                if (this.pasteFilter != undefined) {
                    // Configure paste filtering, the default is false
                    this.editor.customConfig.pasteFilter = this.pasteFilter
                }
                if (this.codeDefaultLang != undefined) {
                    // Default code type, default javascript
                    this.editor.customConfig.codeDefaultLang = this.codeDefaultLang
                }
                if (this.hideLinkImg != undefined) {
                    // Hide the function of adding network pictures, which is displayed by default
                    // this.editor.customConfig.hideLinkImg = this.hideLinkImg
                }
                if (this.uploadImgParams != undefined && this.uploadImgParams instanceof Object) {
                    // Upload image custom parameters
                    // this.editor.customConfig.uploadImgParams = this.uploadImgParams
                }
                if (this.uploadImgHeaders != undefined && this.uploadImgHeaders instanceof Object) {
                    // Upload an image to customize the header
                    // this.editor.customConfig.uploadImgHeaders = this.uploadImgHeaders
                }
                if (this.value) {
                    this.setHtml(this.value)
                }
                if (this.disabled) {
                    this.disable()
                }
                this.listenChange()
            },
            //Filter Editor Menu
            filterMenu(menus) {
                return _.map(menus, (item, key) => {
                    if (item === 'source') {
                        return null
                    }
                    return item
                })
            },
            // Filter unavailable menu
            filterDisabledMenu(menus, disabledMenus) {
                let menusToString = menus.join(',');
                _(disabledMenus).forEach((res) => {
                    menusToString = menusToString.replace(res, '').replace(',,', ',')
                });
                if (menusToString.length && menusToString[0] == ',') {
                    menusToString.substr(1, menusToString.length)
                }
                return menusToString.split(',')
            },
            insertImg(url) {
                this.editor.command(null, 'insertHtml', '<img src=' + url + ' style="max-width:100%;"/>')
            },
            //get content (html)
            getHtml() {
                return this.editor.txt.html()
            },
            // Get content (plain text)
            getText() {
                return this.editor.txt.text()
            },
            // Set content (html)
            setHtml(text) {
                this.editor.txt.html(text)
            },
            // Additional content (html)
            appendHtml(text) {
                this.editor.txt.append(text)
            },
            // clear content
            clear() {
                this.editor.clear()
            },
            //enable editor
            enable() {
                this.editor.enable()
            },
            // disable editor
            disable() {
                this.editor.disable()
            },
            // destroy editor
            destroy() {
                this.editor.destroy()
            },
            // restore editor
            undestroy() {
                this.editor.undestroy()
            },
            // Listen for content changes
            listenChange() {
                this.editor.onchange = () => {
                    this.$emit('change');
                    let result = '';
                    if (!this.getText()) {
                        result = this.getHtml()
                    }
                    if (this.isRealtime !== false) {
                        this.$emit('input', result)
                    }
                }
            }
        },
        mounted() {
            this.initSize();
            setTimeout(() => {
                this.createEditor();
            }, 100);
        }
    }
</script>
