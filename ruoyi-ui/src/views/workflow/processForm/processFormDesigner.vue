<template>
  <div>
    <form-designer ref="formDesigner" v-model="designerForm"/>
  </div>
</template>

<script>
export default {
  name: "processFormDesigner",
  data() {
    return {
      designerForm: ''
     }
   }
}
</script>

<style>

</style>