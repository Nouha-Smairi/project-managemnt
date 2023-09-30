import $http from '@/assets/js/http'

/*Favorites*/
export function collect(code, type = 'collect') {
    return $http.post('project/project_collect/collect', {type: type, projectCode: code});
}
