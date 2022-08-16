<template>
  <div>
    <form-designer ref="formDesigner" v-model="designerForm"/>
  </div>
</template>

<script>
import { getProcessForm } from "@/api/workflow/processForm";
export default {
  name: "processFormDesigner",
  data() {
    return {
      designerForm: ''
     }
  },
  mounted() {
    getProcessForm(this.$route.params.id).then(response => {
      this.designerForm = response.data.formDesignerText;
    });
  },
  methods: {
  }
}
</script>

<style>

</style>