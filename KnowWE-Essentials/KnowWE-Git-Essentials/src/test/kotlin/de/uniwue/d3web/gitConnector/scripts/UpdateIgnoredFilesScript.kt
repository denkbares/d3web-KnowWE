package de.uniwue.d3web.gitConnector.scripts

import de.uniwue.d3web.gitConnector.UserData
import de.uniwue.d3web.gitConnector.impl.JGitBackedGitConnector
import java.nio.file.Paths

object UpdateIgnoredFilesScript {

    fun execute(pathToGit: String, gitIgnoreContent: String, pathsToIgnore: List<String>) {
        val connector = JGitBackedGitConnector.fromPath(pathToGit)

        val numBranches = connector.listBranches().size

        connector.listBranches().forEachIndexed { index,branchName ->
            println("branchName: $branchName ($index of $numBranches)")
            val swapSuccessful = connector.switchToBranch(branchName, false)
            if (!swapSuccessful) {
                throw IllegalStateException("Couldn't swap branch $branchName")
            }

            //step 1 overwrite .gitignore
            val gitIgnore = Paths.get(pathToGit, ".gitignore")
            gitIgnore.toFile().writeText(gitIgnoreContent)

            //commit that file
            connector.addPath(".gitignore")
            connector.commitPathsForUser(
                "Update .gitignore",
                "Markus Merged",
                "markus@merged.com",
                setOf("${gitIgnore.fileName}")
            )

            //then we have to untrack everything unwanted in that branch
            val untracked = pathsToIgnore.map { pathToIgnore ->
                pathToIgnore to connector.untrackPath(pathToIgnore)
            }

            //then we have to commit those as well
            connector.commitForUser(
                UserData(
                    "Clara Commit",
                    "clara@commit.com",
                    "Untrack unnecessary files"
                )
            )

        }


    }
}

fun main() {
    val pathToGit = "/Users/mkrug/Konap/konap_wiki_clean/Wiki_VM"


    val ignoreContent = """
           .DS_Store
           *.vof
           */*.vof
           *.wc
           */*.wc
           *.zip
           */*.zip
           ci-build*.xml
           */ci-build*.xml
           KONAP+master-only+SAP-att/*
    """.trimIndent()

    val listToUntrack = listOf(
        //ci stuff
        "KONAP+master-only+Main+QS-att/ci-build-ConfigitCheck_QS.xml",
        "KONAP+master-only+Main+QS-att/ci-build-General_QS.xml",
        "KONAP+master-only+Main+QS-att/ci-build-GeneralChecks.xml",
        "KONAP+km_sp_wd2e+Konsistenz-att/ci-build-sp-WD2e-QS.xml",
        "KONAP+km_xx_wd13+Konsistenz-att/ci-build-xx-WD13-QS.xml",
        "KONAP+km_sp_wd2d+Konsistenz-att/ci-build-sp-WD2d-QS.xml",
        "KONAP+sp+Configit+Bridge-att/ci-build-ConfigitCheck_SP_compile.xml",
        "KONAP+sp+Configit+Bridge-att/ci-build-ConfigitCheck_SP.xml",
        "KONAP+sp+Configit+Bridge-att/ci-build-ConfigitCheck_SP_BlockedValues.xml",
        "KONAP+km_sp_wd2c+Konsistenz-att/ci-build-sp-WD2c-QS.xml",
        "KONAP+wm_sp_wd12c+Konsistenz-att/ci-build-sp-WD12c-QS.xml",
        "KONAP+wm_sp_wd12+Konsistenz-att/ci-build-sp-WD12-QS.xml",
        "KONAP+wm_sp_wd12d+Konsistenz-att/ci-build-sp-WD12d-QS.xml",
        "KONAP+wm_sp_wd12e+Konsistenz-att/ci-build-sp-WD12e-QS.xml",
        "KONAP+wm_xx_merkmale+Konsistenz-att/ci-build-xx-WM_Merkmale-QS.xml",
        "KONAP+km_xx_wd2a3+Konsistenz-att/ci-build-xx-WD2a3-QS.xml",
        "KONAP+km_sp_wd6+Konsistenz-att/ci-build-sp-WD6-QS.xml",
        "KONAP+km_sp_wd4+Konsistenz-att/ci-build-sp-WD4-QS.xml",
        "KONAP+km_xx_merkmale+Konsistenz-att/ci-build-xx-KM_Merkmale-QS.xml",
        "KONAP+km_sp_wd6b+Konsistenz-att/ci-build-sp-WD6b-QS.xml",
        "KONAP+km_sp_wd6c+Konsistenz-att/ci-build-sp-WD6c-QS.xml",
        "KONAP+km_sp_wd5+Konsistenz-att/ci-build-sp-WD5-QS.xml",
        "KONAP+vm_sp_wd14+Konsistenz-att/ci-build-sp-WD14-QS.xml",
        "KONAP+vm_xx_wd3a+Konsistenz-att/ci-build-xx-WD3a-QS.xml",
        "KONAP+vm_sp_wd2b+Konsistenz-att/ci-build-sp-WD2b-QS.xml",
        "KONAP+vm_sp_wd3b+Konsistenz-att/ci-build-sp-WD3b-QS.xml",
        "KONAP+vm_xx_wd7a+Konsistenz-att/ci-build-xx-WD7a-QS.xml",
        "KONAP+vm_xx_wd2a1+Konsistenz-att/ci-build-xx-WD2a1-QS.xml",
        "KONAP+vm_sp_vmm150+Konsistenz-att/ci-build-sp-VMM150-QS-manuell.xml",
        "KONAP+vm_sp_vmm150+Konsistenz-att/ci-build-sp-VMM150-QS.xml",
        "KONAP+vm_sp_wd2a2+Konsistenz-att/ci-build-sp-WD2a2-QS.xml",
        "KONAP+vm_xx_merkmale+Konsistenz-att/ci-build-xx-VM_Merkmale-QS.xml",
        "KONAP+vm_sp_wd7d+Konsistenz-att/ci-build-sp-WD7d-QS.xml",

        //vof files
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_MegaTIP.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_MegaBahnDUV.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_MegaBahn.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_MegaBahnTIP.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_ProfiBahnTIP.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_ProfiWLT.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_ProfiTIP.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_MegaBasis.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_MegaBahnWLT.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_ProfiBahn.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_MegaWLT.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_ProfiBahnWLT.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche_VM_GM_SP_Var_ProfiBasis.vof",
        "KONAP+sp+Configit+Bridge-att/Sattelpritsche.vof",
        //other
        ".DS_Store"
    )

    UpdateIgnoredFilesScript.execute(pathToGit, ignoreContent, listToUntrack)


}