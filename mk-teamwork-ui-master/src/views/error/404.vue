<template>
    <div class="page-403">
        <ErrorPage
                code="404"
                desc="Sorry, the page you visited does not exist"
        >
            <img slot="img" src="../../assets/image/error/404.svg" alt="">
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
