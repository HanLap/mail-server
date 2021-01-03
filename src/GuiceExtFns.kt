package lappe.xyz.server

import com.google.inject.AbstractModule
import com.google.inject.binder.AnnotatedBindingBuilder
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder



infix fun <T> LinkedBindingBuilder<T>.to(implementation: Class<out T?>?): ScopedBindingBuilder? = this.to(implementation)
