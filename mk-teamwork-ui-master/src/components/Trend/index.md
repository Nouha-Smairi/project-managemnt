# Trend trend marker

Trend symbols, marking up and down trends. Green is usually used to represent "good" and red to represent "bad", except for stock ups and downs.



Citation:

```javascript
import Trend from '@/components/Trend'

export default {
    components: {
        Trend
    }
}
```



## code demo  [demo](https://pro.loacg.com/test/home)

```html
<trend flag="up">5%</trend>
```
或
```html
<trend flag="up">
    <span slot="term">salary</span>
    5%
</trend>
```
或
```html
<trend flag="up" term="工资">5%</trend>
```


## API

|parameter    | illustrate                                      |type         |Defaults |
|----------|------------------------------------------|-------------|-------|
| flag | Rise and fall sign：`up|down` | string | - |
| reverseColor | color inversion| Boolean | false |

