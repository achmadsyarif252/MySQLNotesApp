package com.syarif.mynotesapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.syarif.mynotesapp.adapter.NoteAdapter
import com.syarif.mynotesapp.databinding.ActivityMainBinding
import com.syarif.mynotesapp.db.NoteHelper
import com.syarif.mynotesapp.entity.Note
import com.syarif.mynotesapp.helper.MappingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteAdapter

    val resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.data != null) {
                when (result.resultCode) {
                    NoteAddUpdateActivity.RESULT_ADD -> {
                        val note =
                            result?.data?.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE) as Note
                        adapter.addItem(note)
                        binding.rvNotes.smoothScrollToPosition(adapter.itemCount - 1)
                        showSnackBarMessage("Satu item berhasil ditambahkan")
                    }

                    NoteAddUpdateActivity.RESULT_UPDATE -> {
                        val note =
                            result?.data?.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE) as Note
                        val position = result.data?.getIntExtra(
                            NoteAddUpdateActivity.EXTRA_POSITION,
                            0
                        ) as Int
                        adapter.updateItem(position, note)
                        binding.rvNotes.smoothScrollToPosition(position)
                        showSnackBarMessage("Satu item berhasil diubah")
                    }

                    NoteAddUpdateActivity.RESULT_DELETE -> {
                        val position =
                            result.data?.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0) as Int
                        adapter.removeItem(position)
                        showSnackBarMessage("Satu item berhasil dihapus")
                    }
                }
            }
        }

    private fun showSnackBarMessage(message: String) {
        Snackbar.make(binding.rvNotes, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Notes"
        supportActionBar?.show()

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.setHasFixedSize(true)

        adapter = NoteAdapter(object : NoteAdapter.OnItemClickCallback {
            override fun onItemClicked(selectedNote: Note?, position: Int) {
                val intent = Intent(this@MainActivity, NoteAddUpdateActivity::class.java)
                intent.putExtra(NoteAddUpdateActivity.EXTRA_NOTE, selectedNote)
                intent.putExtra(NoteAddUpdateActivity.EXTRA_POSITION, position)
                resultLauncher.launch(intent)
            }
        })

        binding.rvNotes.adapter = adapter
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, NoteAddUpdateActivity::class.java)
            resultLauncher.launch(intent)
        }

        loadNotesAsync()

        if (savedInstanceState == null) {
            loadNotesAsync()
        } else {
            val list = savedInstanceState.getParcelableArrayList<Note>(EXTRA_STATE)
            if (list != null) {
                adapter.listNotes = list
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listNotes)
    }

    private fun loadNotesAsync() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val noteHelper = NoteHelper.getInstance(applicationContext)
            noteHelper.open()
            val defferedNotes = async(Dispatchers.IO) {
                val cursor = noteHelper.queryAll()
                MappingHelper.mapCursorToArrayList(cursor)
            }
            binding.progressBar.visibility = View.INVISIBLE
            val notes = defferedNotes.await()
            if (notes.size > 0) {
                adapter.listNotes = notes
            } else {
                adapter.listNotes = ArrayList()
                showSnackBarMessage("Tidak ada data saat ini")
            }
            noteHelper.close()
        }
    }


    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }

}