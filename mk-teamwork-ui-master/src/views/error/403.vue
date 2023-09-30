<template>
    <div class="page-403">
        <ErrorPage
                code="403"
                desc="Sorry, you do not have permission to access this page"
        >
            <img slot="img" src="../../assets/image/error/403.svg" alt="">
        </ErrorPage>
    </div>
</template>
<script>
    import ErrorPage from '../../components/error/errorPage';
    export default {
        components: {
            ErrorPage
        }
    }
</script>
