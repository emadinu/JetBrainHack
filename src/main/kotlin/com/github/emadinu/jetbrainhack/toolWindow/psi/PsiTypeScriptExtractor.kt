package com.github.emadinu.jetbrainhack.toolWindow.psi

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.json.JSONArray
import org.json.JSONObject

object PsiTypeScriptExtractor {

    fun extractTypeScriptClasses(project: Project): JSONArray {
        val result = JSONArray()

        val tsFiles: Collection<VirtualFile> = FilenameIndex.getAllFilesByExt(
            project,
            "ts",
            GlobalSearchScope.projectScope(project)
        )

        val psiManager = PsiManager.getInstance(project)

        for (file in tsFiles) {
            val psiFile = psiManager.findFile(file) ?: continue

            // Caută clase
            val typeScriptClasses = PsiTreeUtil.findChildrenOfType(psiFile, TypeScriptClass::class.java)
            for (cls in typeScriptClasses) {
                result.put(psiElementToJson(cls.name, PsiTreeUtil.findChildrenOfType(cls, TypeScriptField::class.java)))
            }

            // Caută interfețe
            val interfaces = PsiTreeUtil.findChildrenOfType(psiFile, TypeScriptInterface::class.java)
            for (intf in interfaces) {
                result.put(psiElementToJson(intf.name, PsiTreeUtil.findChildrenOfType(intf, TypeScriptField::class.java)))
            }
        }

        return result
    }

    private fun psiElementToJson(name: String?, fields: Collection<TypeScriptField>): JSONObject {
        val classJson = JSONObject()
        classJson.put("className", name ?: "Unnamed")

        val fieldNames = JSONArray()
        for (field in fields) {
            val fieldName = field.name
            if (!fieldName.isNullOrEmpty()) {
                fieldNames.put(fieldName)
            }
        }

        classJson.put("fields", fieldNames)
        return classJson
    }
}
