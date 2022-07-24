package com.grandfatherpikhto.lessonble02.helper

import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.grandfatherpikhto.lessonble02.ui.MainActivity

fun linkMenu(menuHost: MenuHost, link: Boolean, menuProvider: MenuProvider) {
    if (link) {
        menuHost.addMenuProvider(menuProvider)
    } else {
        menuHost.removeMenuProvider(menuProvider)
    }
}

fun MainActivity.linkMenu(link: Boolean, menuProvider: MenuProvider)
    = linkMenu(this as MenuHost, link, menuProvider)
fun Fragment.linkMenu(link: Boolean, menuProvider: MenuProvider)
    = linkMenu(requireActivity() as MenuHost, link, menuProvider)