package com.example.firebase_crudb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

data class Note(val id: String = "", val title: String = "")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FirestoreCrudApp()
            }
        }
    }
}

@Composable
fun FirestoreCrudApp() {
    val db = Firebase.firestore
    val notesCollection = db.collection("notes")

    var textInput by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    var noteList by remember { mutableStateOf(listOf<Note>()) }

    // READ: Lắng nghe dữ liệu thay đổi theo thời gian thực (Realtime)
    LaunchedEffect(Unit) {
        notesCollection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                noteList = snapshot.documents.map { doc ->
                    Note(id = doc.id, title = doc.getString("title") ?: "")
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text(if (editingId == null) "Nhập ghi chú mới" else "Sửa ghi chú") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (textInput.isNotBlank()) {
                    if (editingId == null) {
                        // CREATE: Thêm mới
                        notesCollection.add(mapOf("title" to textInput))
                    } else {
                        // UPDATE: Cập nhật
                        notesCollection.document(editingId!!).update("title", textInput)
                        editingId = null
                    }
                    textInput = ""
                }
            }) {
                Text(if (editingId == null) "Thêm" else "Lưu")
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(noteList) { note ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(note.title, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            textInput = note.title
                            editingId = note.id
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Sửa")
                        }
                        IconButton(onClick = {
                            // DELETE: Xóa document
                            notesCollection.document(note.id).delete()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa")
                        }
                    }
                }
            }
        }
    }
}